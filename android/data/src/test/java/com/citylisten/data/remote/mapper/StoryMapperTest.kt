package com.citylisten.data.remote.mapper

import com.citylisten.data.remote.dto.StoryDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class StoryMapperTest {
    @Test
    fun `maps dto to domain correctly`() {
        val dto = StoryDto("s1", "p1", "Title", "Text", "ru", "2024-01-01T12:00:00Z")
        val domain = dto.toDomain()

        assertEquals(dto.id, domain.id)
        assertEquals(dto.title, domain.title)
        assertNotNull(domain.createdAt)
    }
}