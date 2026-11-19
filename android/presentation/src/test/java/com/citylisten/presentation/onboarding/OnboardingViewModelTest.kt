package com.citylisten.presentation.onboarding

import com.citylisten.domain.usecase.CompleteOnboardingUseCase
import com.citylisten.presentation.MainDispatcherRule
import com.citylisten.presentation.testing.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `completing onboarding stores the flag and emits completion`() = runTest {
        val repository = FakeSettingsRepository(onboardingCompleted = false)
        val viewModel = OnboardingViewModel(CompleteOnboardingUseCase(repository))

        assertFalse(viewModel.completed.value)

        viewModel.complete()

        assertTrue(repository.onboardingCompleted)
        assertTrue(viewModel.completed.value)
    }
}
