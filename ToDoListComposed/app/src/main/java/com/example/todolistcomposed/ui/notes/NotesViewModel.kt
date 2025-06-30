package com.example.todolistcomposed.ui.notes

import androidx.lifecycle.ViewModel
import com.example.todolistcomposed.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val taskRepository: TaskRepository // Hilt injects THE SAME TaskRepository instance
) : ViewModel() {

    // Fetch only tasks that are considered "notes"
    // This would ideally be a specific query in DAO and method in Repository
    // fun getNotes() = taskRepository.getAllNotes()
}