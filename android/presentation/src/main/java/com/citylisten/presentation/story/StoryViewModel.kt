package com.citylisten.presentation.story

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citylisten.domain.usecase.GetStoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getStory: GetStoryUseCase
) : ViewModel() {

    private val storyId: String = checkNotNull(savedStateHandle[STORY_ID_ARG]) {
        "Не передан идентификатор рассказа"
    }

    private val _uiState = MutableStateFlow<StoryUiState>(StoryUiState.Loading)
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val story = runCatching { getStory(storyId) }.getOrNull()
            _uiState.value = story?.let(StoryUiState::Content) ?: StoryUiState.NotFound
        }
    }

    companion object {
        const val STORY_ID_ARG = "storyId"
    }
}
