package com.citylisten.domain.usecase

import com.citylisten.domain.fake.FakeSettingsRepository
import com.citylisten.domain.result.DomainErrorType
import com.citylisten.domain.result.DomainResult
import com.citylisten.domain.validation.DisplayNameValidator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveProfileUseCaseTest {

    @Test
    fun `saves normalized display name`() = runTest {
        val repository = FakeSettingsRepository(profileName = "")
        val useCase = SaveProfileUseCase(repository, DisplayNameValidator())

        val result = useCase("  Анна   Мария ")

        assertEquals(DomainResult.Success("Анна Мария"), result)
        assertEquals("Анна Мария", repository.profileName)
    }

    @Test
    fun `does not save invalid name`() = runTest {
        val repository = FakeSettingsRepository(profileName = "Старое имя")
        val useCase = SaveProfileUseCase(repository, DisplayNameValidator())

        val result = useCase("А")

        assertTrue(result is DomainResult.Failure)
        assertEquals(
            DomainErrorType.INVALID_INPUT,
            (result as DomainResult.Failure).error.type
        )
        assertEquals("Старое имя", repository.profileName)
    }

    @Test
    fun `does not save blank name`() = runTest {
        val repository = FakeSettingsRepository(profileName = "")
        val useCase = SaveProfileUseCase(repository, DisplayNameValidator())

        val result = useCase("   ")

        assertTrue(result is DomainResult.Failure)
        assertEquals("", repository.profileName)
    }
}
