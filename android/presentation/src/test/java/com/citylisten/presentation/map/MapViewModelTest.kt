package com.citylisten.presentation.map

import app.cash.turbine.test
import com.citylisten.domain.model.Place
import com.citylisten.domain.model.Story
import com.citylisten.domain.repository.StoryRepository
import com.citylisten.domain.usecase.GetHistoryUseCase
import com.citylisten.presentation.MainDispatcherRule
import com.citylisten.presentation.ui.UiError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

class MapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `saved stories become places without duplicates`() = runTest {
        val latest = story(id = "latest", placeId = "osm:park", createdAt = "2026-11-18T11:32:00Z")
        val older = story(id = "older", placeId = "osm:park", createdAt = "2026-11-18T10:32:00Z")
        val cafe = story(id = "cafe", placeId = "osm:cafe", createdAt = "2026-11-18T09:05:00Z")
        val viewModel = createViewModel(
            FakeStoryRepository(MutableStateFlow(listOf(latest, older, cafe)))
        )

        assertEquals(MapUiState.Loading, viewModel.uiState.value)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is MapUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(MapUiState.Content(listOf(latest, cafe)), state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty history is emitted as empty state`() = runTest {
        val viewModel = createViewModel(FakeStoryRepository(MutableStateFlow(emptyList())))

        assertEquals(MapUiState.Loading, viewModel.uiState.value)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is MapUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(MapUiState.Empty, state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `network failure is mapped to error state`() = runTest {
        val viewModel = createViewModel(
            FakeStoryRepository(flow { throw IOException("Нет сети") })
        )

        assertEquals(MapUiState.Loading, viewModel.uiState.value)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is MapUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(MapUiState.Error(UiError.NETWORK), state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry loads places again after error`() = runTest {
        val repository = FakeStoryRepository(flow { throw IOException("Нет сети") })
        val viewModel = createViewModel(repository)
        val story = story(id = "after-retry")

        assertEquals(MapUiState.Loading, viewModel.uiState.value)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is MapUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(MapUiState.Error(UiError.NETWORK), state)

            repository.history = MutableStateFlow(listOf(story))
            viewModel.retry()

            state = awaitItem()
            while (state is MapUiState.Loading || state is MapUiState.Error) {
                state = awaitItem()
            }
            assertEquals(MapUiState.Content(listOf(story)), state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(repository: FakeStoryRepository): MapViewModel =
        MapViewModel(GetHistoryUseCase(repository))

    private fun story(
        id: String,
        placeId: String = "osm:$id",
        createdAt: String = "2026-11-18T10:00:00Z"
    ): Story = Story(
        id = id,
        placeId = placeId,
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
        error("generate() не используется в тестах карты")

    override suspend fun save(story: Story) = Unit

    override suspend fun getCached(placeId: String, language: String): Story? = null

    override fun observeHistory(): Flow<List<Story>> = history
}
