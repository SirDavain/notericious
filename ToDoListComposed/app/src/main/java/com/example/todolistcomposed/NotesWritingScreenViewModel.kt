package com.example.todolistcomposed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotesWritingScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<NotesWritingScreenUiState>(NotesWritingScreenUiState())
    val uiState: StateFlow<NotesWritingScreenUiState> = _uiState

    fun performSomeAction() {
        viewModelScope.launch {
            // Update uiState
            _uiState.value = _uiState.value.copy(isLoading = true)
            // ... do some work ...
            _uiState.value = _uiState.value.copy(isLoading = false, data = "Updated Data")
        }
    }
}

data class NotesWritingScreenUiState(
    val isLoading: Boolean = false,
    val data: String? = null
    // Add other state properties
)