package com.citylisten.presentation.history

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
import java.time.ZoneId

class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `stories are grouped by day and sorted newest first`() = runTest {
        val older = story(id = "older", createdAt = "2026-11-17T16:05:00Z")
        val latest = story(id = "latest", createdAt = "2026-11-18T11:32:00Z")
        val middle = story(id = "middle", createdAt = "2026-11-18T11:18:00Z")
        val viewModel = createViewModel(
            FakeStoryRepository(MutableStateFlow(listOf(older, latest, middle)))
        )

        assertEquals(HistoryUiState.Loading, viewModel.uiState.value)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is HistoryUiState.Loading) {
                state = awaitItem()
            }
            val content = state as HistoryUiState.Content
            assertEquals(
                listOf(dayOf(latest), dayOf(older)),
                content.days.map { it.date }
            )
            assertEquals(listOf(latest, middle), content.days.first().stories)
            assertEquals(listOf(older), content.days.last().stories)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty history is emitted as empty state`() = runTest {
        val viewModel = createViewModel(FakeStoryRepository(MutableStateFlow(emptyList())))

        assertEquals(HistoryUiState.Loading, viewModel.uiState.value)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is HistoryUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(HistoryUiState.Empty, state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `network failure without cache is mapped to error state`() = runTest {
        val viewModel = createViewModel(
            FakeStoryRepository(flow { throw IOException("Нет сети") })
        )

        assertEquals(HistoryUiState.Loading, viewModel.uiState.value)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is HistoryUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(
                HistoryUiState.Error(error = UiError.NETWORK, cachedDays = emptyList()),
                state
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failure after loaded data keeps cached days`() = runTest {
        val story = story(id = "cached", createdAt = "2026-11-18T11:32:00Z")
        val viewModel = createViewModel(
            FakeStoryRepository(
                flow {
                    emit(listOf(story))
                    throw IOException("Нет сети")
                }
            )
        )

        assertEquals(HistoryUiState.Loading, viewModel.uiState.value)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is HistoryUiState.Loading || state is HistoryUiState.Content) {
                state = awaitItem()
            }
            val error = state as HistoryUiState.Error
            assertEquals(UiError.NETWORK, error.error)
            assertEquals(listOf(story), error.cachedDays.flatMap { it.stories })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry loads history again after error`() = runTest {
        val repository = FakeStoryRepository(flow { throw IOException("Нет сети") })
        val viewModel = createViewModel(repository)
        val story = story(id = "after-retry")

        assertEquals(HistoryUiState.Loading, viewModel.uiState.value)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is HistoryUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(
                HistoryUiState.Error(error = UiError.NETWORK, cachedDays = emptyList()),
                state
            )

            repository.history = MutableStateFlow(listOf(story))
            viewModel.retry()

            state = awaitItem()
            while (state is HistoryUiState.Loading || state is HistoryUiState.Error) {
                state = awaitItem()
            }
            val content = state as HistoryUiState.Content
            assertEquals(listOf(story), content.days.flatMap { it.stories })
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(repository: FakeStoryRepository): HistoryViewModel =
        HistoryViewModel(GetHistoryUseCase(repository))

    private fun dayOf(story: Story) = story.createdAt.atZone(ZoneId.systemDefault()).toLocalDate()

    private fun story(
        id: String,
        createdAt: String = "2026-11-18T10:00:00Z"
    ): Story = Story(
        id = id,
        placeId = "osm:$id",
        title = "Рассказ $id",
        text = "Текст рассказа $id",
        language = "ru",
        createdAt = Instant.parse(createdAt),
        cached = false
    )
}

private class FakeStoryRepository(
    var history: Flow<List<Story>> = MutableStateFlow(emptyList())
) : StoryRepository {

    override suspend fun generate(place: Place, language: String): Story =
        error("generate() не используется в тестах истории")

    override suspend fun save(story: Story) = Unit

    override suspend fun getCached(placeId: String, language: String): Story? = null

    override fun observeHistory(): Flow<List<Story>> = history
}
