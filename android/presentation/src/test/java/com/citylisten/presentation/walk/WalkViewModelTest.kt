package com.citylisten.presentation.walk

import app.cash.turbine.test
import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.model.Place
import com.citylisten.domain.model.Story
import com.citylisten.domain.repository.StoryRepository
import com.citylisten.domain.result.DomainError
import com.citylisten.domain.result.DomainErrorType
import com.citylisten.domain.result.DomainResult
import com.citylisten.domain.usecase.GetAddressUseCase
import com.citylisten.domain.usecase.GetLastStoryUseCase
import com.citylisten.domain.usecase.ObserveLanguageUseCase
import com.citylisten.domain.usecase.ObserveLocationConsentUseCase
import com.citylisten.domain.usecase.StartWalkUseCase
import com.citylisten.presentation.MainDispatcherRule
import com.citylisten.presentation.testing.FakeAddressRepository
import com.citylisten.presentation.testing.FakeLocationRepository
import com.citylisten.presentation.testing.FakeSettingsRepository
import com.citylisten.presentation.ui.UiError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class WalkViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `content state contains latest story and consent`() = runTest {
        val older = story(id = "old", createdAt = "2026-11-16T10:00:00Z")
        val latest = story(id = "new", createdAt = "2026-11-17T10:00:00Z")
        val repository = FakeStoryRepository(
            history = MutableStateFlow(listOf(older, latest))
        )
        val viewModel = createViewModel(repository, consent = true)

        assertEquals(WalkUiState.Loading, viewModel.uiState.value)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is WalkUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(
                WalkUiState.Content(story = latest, locationConsent = true),
                state
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty state is emitted when story history is empty`() = runTest {
        val viewModel = createViewModel(
            repository = FakeStoryRepository(history = MutableStateFlow(emptyList())),
            consent = false
        )

        assertEquals(WalkUiState.Loading, viewModel.uiState.value)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is WalkUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(WalkUiState.Empty(locationConsent = false), state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `network failure is mapped to error state`() = runTest {
        val viewModel = createViewModel(
            repository = FakeStoryRepository(
                history = flow { throw IOException("Нет сети") }
            )
        )

        assertEquals(WalkUiState.Loading, viewModel.uiState.value)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is WalkUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(WalkUiState.Error(WalkError.NETWORK), state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry loads data again after error`() = runTest {
        val repository = FakeStoryRepository(
            history = flow { throw IOException("Нет сети") }
        )
        val viewModel = createViewModel(repository)
        val story = story(id = "after-retry")

        assertEquals(WalkUiState.Loading, viewModel.uiState.value)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is WalkUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(WalkUiState.Error(WalkError.NETWORK), state)

            repository.history = MutableStateFlow(listOf(story))
            viewModel.retry()

            state = awaitItem()
            while (state is WalkUiState.Loading || state is WalkUiState.Error) {
                state = awaitItem()
            }
            assertEquals(
                WalkUiState.Content(story = story, locationConsent = true),
                state
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `start walk collects gps coordinates`() = runTest {
        val locationRepository = FakeLocationRepository()
        val viewModel = createViewModel(
            repository = FakeStoryRepository(),
            locationRepository = locationRepository
        )
        val point = GeoPoint(55.75, 37.61)

        viewModel.startWalk()
        assertTrue(viewModel.locationState.value.isWalking)
        runCurrent()

        locationRepository.emit(point)
        advanceUntilIdle()

        assertEquals(point, viewModel.locationState.value.point)
        assertTrue(viewModel.locationState.value.isWalking)
        viewModel.stopWalk()
    }

    @Test
    fun `stop walk clears gps state`() = runTest {
        val viewModel = createViewModel(
            repository = FakeStoryRepository(),
            locationRepository = FakeLocationRepository(
                initialPoints = listOf(GeoPoint(55.75, 37.61))
            )
        )

        viewModel.startWalk()
        advanceUntilIdle()
        viewModel.stopWalk()

        assertEquals(WalkLocationUiState(), viewModel.locationState.value)
        assertFalse(viewModel.locationState.value.isWalking)
    }

    @Test
    fun `pause keeps coordinates and stops updates until resume`() = runTest {
        val locationRepository = FakeLocationRepository()
        val viewModel = createViewModel(
            repository = FakeStoryRepository(),
            locationRepository = locationRepository
        )
        val first = GeoPoint(55.75, 37.61)
        val ignored = GeoPoint(55.76, 37.62)
        val resumed = GeoPoint(55.77, 37.63)

        viewModel.startWalk()
        runCurrent()
        locationRepository.emit(first)
        advanceUntilIdle()
        assertEquals(first, viewModel.locationState.value.point)

        viewModel.pauseWalk()
        advanceUntilIdle()
        locationRepository.emit(ignored)
        advanceUntilIdle()

        assertTrue(viewModel.locationState.value.isWalking)
        assertEquals(first, viewModel.locationState.value.point)

        viewModel.resumeWalk()
        runCurrent()
        locationRepository.emit(resumed)
        advanceUntilIdle()

        assertTrue(viewModel.locationState.value.isWalking)
        assertEquals(resumed, viewModel.locationState.value.point)
        viewModel.stopWalk()
    }

    @Test
    fun `resume does not start walk before it was started`() = runTest {
        val viewModel = createViewModel(
            repository = FakeStoryRepository(),
            locationRepository = FakeLocationRepository(
                initialPoints = listOf(GeoPoint(55.75, 37.61))
            )
        )

        viewModel.resumeWalk()
        advanceUntilIdle()

        assertFalse(viewModel.locationState.value.isWalking)
        assertEquals(WalkLocationUiState(), viewModel.locationState.value)
    }

    @Test
    fun `unavailable location becomes unavailable error`() = runTest {
        val viewModel = createViewModel(
            repository = FakeStoryRepository(),
            locationRepository = FakeLocationRepository(available = false)
        )

        viewModel.startWalk()
        advanceUntilIdle()

        assertEquals(
            WalkLocationError.UNAVAILABLE,
            viewModel.locationState.value.error
        )
        assertFalse(viewModel.locationState.value.isWalking)
    }

    @Test
    fun `security failure becomes permission error`() = runTest {
        val viewModel = createViewModel(
            repository = FakeStoryRepository(),
            locationRepository = FakeLocationRepository(
                failure = SecurityException("no permission")
            )
        )

        viewModel.startWalk()
        advanceUntilIdle()

        assertEquals(
            WalkLocationError.PERMISSION,
            viewModel.locationState.value.error
        )
        assertFalse(viewModel.locationState.value.isWalking)
    }

    @Test
    fun `loads address for first gps point`() = runTest {
        val locationRepository = FakeLocationRepository()
        val addressRepository = FakeAddressRepository()
        val viewModel = createViewModel(
            repository = FakeStoryRepository(),
            locationRepository = locationRepository,
            addressRepository = addressRepository
        )
        val point = GeoPoint(55.75, 37.61)

        viewModel.startWalk()
        runCurrent()
        locationRepository.emit(point)
        advanceUntilIdle()

        assertEquals(
            WalkAddressUiState.Content(FakeAddressRepository.ADDRESS),
            viewModel.addressState.value
        )
        assertEquals(listOf(point), addressRepository.requests)
        assertEquals(listOf("ru"), addressRepository.languages)

        viewModel.stopWalk()
        assertEquals(WalkAddressUiState.Idle, viewModel.addressState.value)
    }

    @Test
    fun `address failure shows retry and reloads the same point`() = runTest {
        val locationRepository = FakeLocationRepository()
        val addressRepository = FakeAddressRepository(
            result = DomainResult.Failure(
                DomainError(DomainErrorType.NETWORK, "Нет сети")
            )
        )
        val viewModel = createViewModel(
            repository = FakeStoryRepository(),
            locationRepository = locationRepository,
            addressRepository = addressRepository
        )
        val point = GeoPoint(55.75, 37.61)

        viewModel.startWalk()
        runCurrent()
        locationRepository.emit(point)
        advanceUntilIdle()

        assertEquals(
            WalkAddressUiState.Error(UiError.NETWORK),
            viewModel.addressState.value
        )

        addressRepository.result = DomainResult.Success(FakeAddressRepository.ADDRESS)
        viewModel.retryAddress()
        advanceUntilIdle()

        assertEquals(
            WalkAddressUiState.Content(FakeAddressRepository.ADDRESS),
            viewModel.addressState.value
        )
        assertEquals(listOf(point, point), addressRepository.requests)
        viewModel.stopWalk()
    }

    @Test
    fun `address refreshes only after one hundred meters`() = runTest {
        val locationRepository = FakeLocationRepository()
        val addressRepository = FakeAddressRepository()
        val viewModel = createViewModel(
            repository = FakeStoryRepository(),
            locationRepository = locationRepository,
            addressRepository = addressRepository
        )
        val first = GeoPoint(55.75, 37.61)
        val near = GeoPoint(55.75045, 37.61)
        val far = GeoPoint(55.75135, 37.61)

        viewModel.startWalk()
        runCurrent()
        locationRepository.emit(first)
        advanceUntilIdle()
        locationRepository.emit(near)
        advanceUntilIdle()
        locationRepository.emit(far)
        advanceUntilIdle()

        assertEquals(listOf(first, far), addressRepository.requests)
        viewModel.stopWalk()
    }

    private fun createViewModel(
        repository: FakeStoryRepository,
        consent: Boolean = true,
        locationRepository: FakeLocationRepository = FakeLocationRepository(),
        addressRepository: FakeAddressRepository = FakeAddressRepository()
    ): WalkViewModel {
        val settingsRepository = FakeSettingsRepository(consent = consent)
        return WalkViewModel(
            getLastStory = GetLastStoryUseCase(repository),
            observeLocationConsent = ObserveLocationConsentUseCase(settingsRepository),
            startWalkUseCase = StartWalkUseCase(locationRepository, settingsRepository),
            getAddress = GetAddressUseCase(addressRepository),
            observeLanguage = ObserveLanguageUseCase(settingsRepository)
        )
    }

    private fun story(
        id: String,
        createdAt: String = "2026-11-17T10:00:00Z"
    ): Story = Story(
        id = id,
        placeId = "place-$id",
        title = "Рассказ $id",
        text = "Текст рассказа $id",
        language = "ru",
        createdAt = Instant.parse(createdAt),
        cached = true
    )
}

private class FakeStoryRepository(
    var history: Flow<List<Story>> = MutableStateFlow(emptyList())
) : StoryRepository {

    override suspend fun generate(place: Place, language: String): Story =
        error("generate() не используется в тестах главного экрана")

    override suspend fun save(story: Story) = Unit

    override suspend fun getCached(placeId: String, language: String): Story? = null

    override fun observeHistory(): Flow<List<Story>> = history
}
