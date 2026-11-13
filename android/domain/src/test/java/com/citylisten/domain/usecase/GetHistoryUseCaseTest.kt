package com.citylisten.domain.usecase

import com.citylisten.domain.fake.FakeStoryRepository
import com.citylisten.domain.model.Story
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class GetHistoryUseCaseTest {

    @Test
    fun `sorts by date and deduplicates`() = runTest {
        val older = Story("s1", "p1", "Old", "Text", "ru", Instant.parse("2024-01-01T10:00:00Z"))
        val newer = Story("s2", "p2", "New", "Text", "ru", Instant.parse("2024-01-02T10:00:00Z"))
        val repo = object : FakeStoryRepository() {
            override fun observeHistory() = MutableStateFlow(listOf(older, newer))
        }
        val useCase = GetHistoryUseCase(repo)

        val result = useCase().first()

        assertEquals("New", result.first().title)
    }

    @Test
    fun `limits to 50 items`() = runTest {
        val stories = (1..100).map {
            Story("s$it", "p$it", "Title $it", "Text", "ru", Instant.now())
        }
        val repo = object : FakeStoryRepository() {
            override fun observeHistory() = MutableStateFlow(stories)
        }
        val useCase = GetHistoryUseCase(repo)

        val result = useCase().first()

        assertEquals(50, result.size)
    }
}