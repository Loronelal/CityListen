package com.citylisten.domain.usecase

import com.citylisten.domain.fake.FakeSettingsRepository
import com.citylisten.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangeLanguageUseCaseTest {

    @Test
    fun `accepts supported language`() = runTest {
        val repo = FakeSettingsRepository(language = "ru")
        val useCase = ChangeLanguageUseCase(repo)

        val result = useCase("en")

        assertTrue(result is DomainResult.Success)
        assertEquals("en", repo.language)
    }

    @Test
    fun `rejects unsupported language`() = runTest {
        val repo = FakeSettingsRepository(language = "ru")
        val useCase = ChangeLanguageUseCase(repo)

        val result = useCase("de")

        assertTrue(result is DomainResult.Failure)
        assertEquals("ru", repo.language)
    }

    @Test
    fun `normalizes uppercase input`() = runTest {
        val repo = FakeSettingsRepository(language = "ru")
        val useCase = ChangeLanguageUseCase(repo)

        useCase("EN")

        assertEquals("en", repo.language)
    }
}