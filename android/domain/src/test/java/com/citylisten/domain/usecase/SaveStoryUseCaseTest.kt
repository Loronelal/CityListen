package com.citylisten.domain.usecase

import com.citylisten.domain.fake.FakeStoryRepository
import com.citylisten.domain.model.Story
import com.citylisten.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class SaveStoryUseCaseTest {

    @Test
    fun `saves valid story`() = runTest {
        val repo = FakeStoryRepository()
        val useCase = SaveStoryUseCase(repo)
        val story = Story("s1", "p1", "Title", "Достаточно длинный текст.", "ru", Instant.now())

        val result = useCase(story)

        assertTrue(result is DomainResult.Success)
        assertEquals(1, repo.savedStories.size)
    }

    @Test
    fun `rejects blank text`() = runTest {
        val repo = FakeStoryRepository()
        val useCase = SaveStoryUseCase(repo)
        val story = Story("s1", "p1", "Title", "", "ru", Instant.now())

        val result = useCase(story)

        assertTrue(result is DomainResult.Failure)
        assertTrue(repo.savedStories.isEmpty())
    }

    @Test
    fun `truncates too-long text`() = runTest {
        val repo = FakeStoryRepository()
        val useCase = SaveStoryUseCase(repo)
        val longText = "a".repeat(SaveStoryUseCase.MAX_TEXT_LENGTH + 100)
        val story = Story("s1", "p1", "Title", longText, "ru", Instant.now())

        useCase(story)

        assertEquals(SaveStoryUseCase.MAX_TEXT_LENGTH, repo.savedStories.first().text.length)
    }
}