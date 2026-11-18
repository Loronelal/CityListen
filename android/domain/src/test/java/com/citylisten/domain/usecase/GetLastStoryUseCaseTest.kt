package com.citylisten.domain.usecase

import com.citylisten.domain.fake.FakeStoryRepository
import com.citylisten.domain.model.Story
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class GetLastStoryUseCaseTest {

    @Test
    fun `returns the newest story`() = runTest {
        val older = Story(
            "s1", "p1", "Старый", "Текст", "ru",
            Instant.parse("2024-01-01T10:00:00Z")
        )
        val newer = Story(
            "s2", "p2", "Новый", "Текст", "ru",
            Instant.parse("2024-01-02T10:00:00Z")
        )
        val repository = FakeStoryRepository(
            history = MutableStateFlow(listOf(older, newer))
        )

        val result = GetLastStoryUseCase(repository)().first()

        assertEquals("Новый", result?.title)
    }

    @Test
    fun `returns null when history is empty`() = runTest {
        val repository = FakeStoryRepository()

        val result = GetLastStoryUseCase(repository)().first()

        assertNull(result)
    }
}
