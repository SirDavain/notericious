package com.example.notericious.ui.notes

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notericious.Task
import com.example.notericious.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val noteId: Int? = savedStateHandle.get<Int>("noteId")

    private val _currentNote = MutableStateFlow<Task?>(null)

    val noteTitle: StateFlow<String> = _currentNote
        .map { it?.title ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val noteContent: StateFlow<String> = _currentNote
        .map { it?.content ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    init {
        if (noteId != null && noteId != 0 && noteId != -1) {
            loadNote(noteId)
        } else {
            // New note or title passed from MainScreen
            val optionalTitle = savedStateHandle.get<String>("optionalTitle")
            _currentNote.value = Task(
                title = optionalTitle ?: "",
                isNote = true,
                completedOrReopenedTimestamp = System.currentTimeMillis()
            )
        }
    }

    private fun loadNote(id: Int) {
        viewModelScope.launch {
            _currentNote.value = taskRepository.getTaskById(id)
        }
    }

    fun updateNoteTitle(newTitle: String) {
        val noteToUpdate = _currentNote.value ?: return
        if (noteToUpdate.title != newTitle) {
            val updatedNote = noteToUpdate.copy(title = newTitle)
            _currentNote.value = updatedNote
            viewModelScope.launch {
                if (updatedNote.id == 0) {
                    taskRepository.insert(updatedNote)
                } else {
                    taskRepository.update(updatedNote)
                }
            }
        }
    }

    fun updateNoteContent(newContent: String) {
        val noteToUpdate = _currentNote.value ?: return
        if (noteToUpdate.content != newContent) {
            val updatedNote = noteToUpdate.copy(content = newContent)
            _currentNote.value = updatedNote
            viewModelScope.launch {
                if (updatedNote.id == 0) {
                    taskRepository.insert(updatedNote)
                } else {
                    taskRepository.update(updatedNote)
                }
            }
        }
    }
}