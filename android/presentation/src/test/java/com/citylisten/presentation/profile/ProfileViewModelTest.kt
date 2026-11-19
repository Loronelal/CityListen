package com.citylisten.presentation.profile

import com.citylisten.domain.usecase.ObserveProfileUseCase
import com.citylisten.domain.usecase.SaveProfileUseCase
import com.citylisten.domain.validation.DisplayNameValidator
import com.citylisten.presentation.MainDispatcherRule
import com.citylisten.presentation.testing.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `prefills saved display name`() = runTest {
        val repository = FakeSettingsRepository(profileName = "Анна")

        val viewModel = createViewModel(repository)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Анна", viewModel.uiState.value.displayName)
        assertEquals("Анна", viewModel.uiState.value.savedName)
    }

    @Test
    fun `saves normalized name`() = runTest {
        val repository = FakeSettingsRepository(profileName = "")
        val viewModel = createViewModel(repository)

        viewModel.onNameChange("  Анна   Мария ")
        viewModel.save()

        assertEquals("Анна Мария", repository.profileName)
        assertEquals("Анна Мария", viewModel.uiState.value.displayName)
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `shows validation error for short name`() = runTest {
        val repository = FakeSettingsRepository(profileName = "Анна")
        val viewModel = createViewModel(repository)

        viewModel.onNameChange("А")
        viewModel.save()

        assertNotNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaved)
        assertEquals("Анна", repository.profileName)
    }

    @Test
    fun `shows validation error for blank name`() = runTest {
        val repository = FakeSettingsRepository(profileName = "")
        val viewModel = createViewModel(repository)

        viewModel.onNameChange("   ")
        viewModel.save()

        assertNotNull(viewModel.uiState.value.error)
        assertEquals("", repository.profileName)
    }

    @Test
    fun `editing clears the previous error and success`() = runTest {
        val repository = FakeSettingsRepository(profileName = "")
        val viewModel = createViewModel(repository)

        viewModel.onNameChange("А")
        viewModel.save()
        assertNotNull(viewModel.uiState.value.error)

        viewModel.onNameChange("Анна")

        assertEquals(null, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaved)
    }

    private fun createViewModel(repository: FakeSettingsRepository): ProfileViewModel {
        return ProfileViewModel(
            observeProfile = ObserveProfileUseCase(repository),
            saveProfile = SaveProfileUseCase(repository, DisplayNameValidator())
        )
    }
}
