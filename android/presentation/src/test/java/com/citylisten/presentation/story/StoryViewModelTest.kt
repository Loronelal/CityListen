package com.citylisten.presentation.story

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.citylisten.domain.model.Place
import com.citylisten.domain.model.Story
import com.citylisten.domain.repository.StoryRepository
import com.citylisten.domain.usecase.GetStoryUseCase
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

class StoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `story is loaded by id from history`() = runTest {
        val story = story("story-1")
        val viewModel = createViewModel("story-1", FakeStoryRepository(MutableStateFlow(listOf(story))))

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is StoryUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(StoryUiState.Content(story), state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `missing story is reported as not found`() = runTest {
        val viewModel = createViewModel(
            storyId = "missing",
            repository = FakeStoryRepository(MutableStateFlow(listOf(story("story-1"))))
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is StoryUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(StoryUiState.NotFound, state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `history failure is reported as not found`() = runTest {
        val repository = FakeStoryRepository(flow { throw IOException("Нет сети") })
        val viewModel = createViewModel("story-1", repository)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is StoryUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(StoryUiState.NotFound, state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(
        storyId: String,
        repository: FakeStoryRepository
    ): StoryViewModel = StoryViewModel(
        savedStateHandle = SavedStateHandle(mapOf(StoryViewModel.STORY_ID_ARG to storyId)),
        getStory = GetStoryUseCase(repository)
    )

    private fun story(id: String): Story = Story(
        id = id,
        placeId = "osm:$id",
        title = "Рассказ $id",
        text = "Текст рассказа $id",
        language = "ru",
        createdAt = Instant.parse("2026-11-18T10:00:00Z"),
        cached = true
    )
}

private class FakeStoryRepository(
    var history: Flow<List<Story>> = MutableStateFlow(emptyList())
) : StoryRepository {

    override suspend fun generate(place: Place, language: String): Story =
        error("generate() не используется в тестах рассказа")

    override suspend fun save(story: Story) = Unit

    override suspend fun getCached(placeId: String, language: String): Story? = null

    override fun observeHistory(): Flow<List<Story>> = history
}
