package com.citylisten.presentation.permission

import com.citylisten.domain.usecase.SetLocationConsentUseCase
import com.citylisten.presentation.MainDispatcherRule
import com.citylisten.presentation.testing.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PermissionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `granted permission updates consent`() = runTest {
        val repository = FakeSettingsRepository(consent = false)
        val viewModel = PermissionViewModel(SetLocationConsentUseCase(repository))

        viewModel.setConsent(true)

        assertTrue(repository.consent)
    }

    @Test
    fun `denied permission clears consent`() = runTest {
        val repository = FakeSettingsRepository(consent = true)
        val viewModel = PermissionViewModel(SetLocationConsentUseCase(repository))

        viewModel.setConsent(false)

        assertFalse(repository.consent)
    }
}
