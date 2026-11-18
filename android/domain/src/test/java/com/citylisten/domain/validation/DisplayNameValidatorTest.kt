package com.citylisten.domain.validation

import com.citylisten.domain.result.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DisplayNameValidatorTest {

    private val validator = DisplayNameValidator()

    @Test
    fun `trims and collapses whitespace`() {
        val result = validator.validate("  Анна   Мария  ")

        assertEquals(DomainResult.Success("Анна Мария"), result)
    }

    @Test
    fun `accepts hyphen and apostrophe`() {
        val result = validator.validate("Анна-Мария O'Neil")

        assertEquals(DomainResult.Success("Анна-Мария O'Neil"), result)
    }

    @Test
    fun `accepts digits`() {
        val result = validator.validate("Гид 42")

        assertEquals(DomainResult.Success("Гид 42"), result)
    }

    @Test
    fun `rejects blank name`() {
        val result = validator.validate("   ")

        assertInvalid(result)
    }

    @Test
    fun `rejects name shorter than two characters`() {
        val result = validator.validate("А")

        assertInvalid(result)
    }

    @Test
    fun `rejects name longer than forty characters`() {
        val result = validator.validate("А".repeat(41))

        assertInvalid(result)
    }

    @Test
    fun `rejects emoji and control characters`() {
        assertInvalid(validator.validate("Анна 🎧"))
        assertInvalid(validator.validate("Анна\nМария"))
    }

    @Test
    fun `rejects name starting with punctuation`() {
        val result = validator.validate(" -Анна")

        assertInvalid(result)
    }

    private fun assertInvalid(result: DomainResult<String>) {
        assertTrue(result is DomainResult.Failure)
    }
}
