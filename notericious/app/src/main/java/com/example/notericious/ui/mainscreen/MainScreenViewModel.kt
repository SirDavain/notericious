package com.example.todolistcomposed.ui.mainscreen

import android.R.attr.text
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolistcomposed.Task
import com.example.todolistcomposed.TaskRepository
import com.example.todolistcomposed.TaskUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskUiState(val id: Int, val text: String, val isDone: Boolean)

fun Task.toUiState(): TaskUiState = TaskUiState(id, text, isDone)

@HiltViewModel
open class MainScreenViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    open val allTasks: StateFlow<List<com.example.todolistcomposed.TaskUiState>> = taskRepository.allTasks
        .map { domainTasks -> domainTasks.map { it.toUiState() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    open var newTaskText by mutableStateOf("")
    open var currentlyEditingTaskId by mutableStateOf<Int?>(null)
    open var currentEditText by mutableStateOf("")

    open fun onNewTaskTextChange(newText: String) {
        newTaskText = newText
    }

    open fun insertNewTask() {
        val text = newTaskText.trim() // Trim whitespace
        if (text.isNotBlank()) {
            viewModelScope.launch {
                val currentTime = System.currentTimeMillis()
                // Create Task object with the new timestamp
                // For new (undone) tasks, this timestamp will place them at the
                // bottom of the "undone" list due to `completedOrReopenedTimestamp ASC`
                // for undone items in the DAO query.
                val taskToInsert = Task(
                    content = text,
                    isDone = false,
                    completedOrReopenedTimestamp = currentTime
                )
                taskRepository.insert(taskToInsert) // Assuming repository.insert takes a Task object
                newTaskText = ""
            }
        }
    }

    // This function will now be responsible for updating the timestamp as well
    open fun updateTaskDoneStatus(taskId: Int, newDoneState: Boolean) {
        val taskBeingModified = allTasks.value.find { it.id == taskId }

        // If the task is currently being edited, save its text content first
        if (currentlyEditingTaskId == taskId) {
            // It's important to use taskBeingModified?.text if available,
            // otherwise currentEditText might be stale if the user didn't type anything new.
            // However, saveEditedTask takes currentEditText. This interaction needs care.
            // For now, let's assume if it's being edited, we save what's in currentEditText.
            saveEditedTask(taskId, currentEditText)
            // After saving, clear editing state for this task
            if (currentlyEditingTaskId == taskId) { // Check again in case saveEditedTask clears it
                currentlyEditingTaskId = null
                currentEditText = ""
            }
        }


        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            // Call the repository method that updates both isDone and the timestamp
            // This assumes your repository has a method like updateTaskDoneStatusAndTimestamp
            // or that your generic update method in the repository handles the Task object correctly.


            // If your repository uses a generic update(task: Task) and you fetch first
            // (More robust as it ensures you're updating the correct full Task object)
            val originalTaskEntity = taskRepository.getTaskById(taskId) // Assuming repository has getTaskById
            if (originalTaskEntity != null) {
                val updatedTaskEntity = originalTaskEntity.copy(
                    isDone = newDoneState,
                    completedOrReopenedTimestamp = currentTime
                )
                taskRepository.update(updatedTaskEntity)
            } else {
                Log.e("TaskViewModel", "Task with ID $taskId not found for updating done status.")
            }
        }
    }

    // --- In-line Editing Logic ---
    open fun startEditingTask(task: TaskUiState) {
        Log.d("ViewModel", "startEditingTask called for ID: ${task.id}")
        currentlyEditingTaskId?.let { oldEditingId ->
            if (oldEditingId != task.id) {
                // Save the previously edited task (if any)
                // This assumes currentEditText holds the text for oldEditingId
                saveEditedTask(oldEditingId, currentEditText)
            }
        }
        currentlyEditingTaskId = task.id
        currentEditText = task.text // Pre-fill with current text
    }

    open fun onCurrentEditTextChange(newText: String) {
        // Only update if a task is actually being edited
        currentEditText = newText
    }

    open fun saveOrDeleteCurrentEditedTask() {
        val editingId = currentlyEditingTaskId
        if (editingId != null) {
            val textToSave = currentEditText
            saveEditedTask(editingId, textToSave) // This will handle blank text as delete
            // Clear editing state AFTER saving/deleting
            currentlyEditingTaskId = null
            currentEditText = ""
        }
    }

    // This method only updates text or deletes. It should NOT modify the completedOrReopenedTimestamp
    // unless the 'isDone' status is also changing here (which it isn't directly).
    private fun saveEditedTask(taskId: Int, newText: String) {
        viewModelScope.launch {
            val trimmedText = newText.trim()
            if (trimmedText.isBlank()) {
                taskRepository.deleteTaskById(taskId)
            } else {
                val originalTaskEntity = taskRepository.getTaskById(taskId)
                if (originalTaskEntity != null) {
                    // Only update the text. Keep existing isDone and completedOrReopenedTimestamp
                    val updatedTaskEntity = originalTaskEntity.copy(text = trimmedText)
                    taskRepository.update(updatedTaskEntity)
                }
            }
        }
    }
}

data class MainScreenUiState(
    val isLoading: Boolean = false,
    val data: String? = null
    // Add other state properties
)