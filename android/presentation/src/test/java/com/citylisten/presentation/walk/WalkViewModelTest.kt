package com.citylisten.presentation.walk

import app.cash.turbine.test
import com.citylisten.domain.model.Place
import com.citylisten.domain.model.Story
import com.citylisten.domain.repository.SettingsRepository
import com.citylisten.domain.repository.StoryRepository
import com.citylisten.domain.usecase.GetLastStoryUseCase
import com.citylisten.domain.usecase.ObserveLocationConsentUseCase
import com.citylisten.presentation.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

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

    private fun createViewModel(
        repository: FakeStoryRepository,
        consent: Boolean = true
    ): WalkViewModel = WalkViewModel(
        getLastStory = GetLastStoryUseCase(repository),
        observeLocationConsent = ObserveLocationConsentUseCase(
            FakeSettingsRepository(consent = consent)
        )
    )

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

private class FakeSettingsRepository(consent: Boolean) : SettingsRepository {

    private val consentFlow = MutableStateFlow(consent)
    private val languageFlow = MutableStateFlow("ru")

    override fun observeLocationConsent(): Flow<Boolean> = consentFlow

    override suspend fun setLocationConsent(consent: Boolean) {
        consentFlow.value = consent
    }

    override fun observeLanguage(): Flow<String> = languageFlow

    override suspend fun setLanguage(language: String) {
        languageFlow.value = language
    }
}
