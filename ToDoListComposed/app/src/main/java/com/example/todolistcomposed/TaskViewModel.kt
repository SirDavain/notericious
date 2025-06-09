package com.example.todolistcomposed // Or your actual package

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.animation.core.copy
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

open class TaskViewModel(
    application: Application,
    private val repository: TaskRepository
) : AndroidViewModel(application) {

    open val allTasks: StateFlow<List<TaskUiState>> = repository.allTasks.map { tasks ->
        // Assuming tasks is List<Task> from DAO
        tasks.map { task ->
            // Ensure TaskUiState constructor matches the properties of your Task entity
            TaskUiState(task.id, task.text, task.isDone /*, task.completionTimestamp if you added it */)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L), // Or SharingStarted.Eagerly if it should start immediately
        initialValue = emptyList()
    )

    open var newTaskText by mutableStateOf("")
    open var currentlyEditingTaskId by mutableStateOf<Int?>(null)
    open var currentEditText by mutableStateOf("")

    open fun onNewTaskTextChange(newText: String) {
        newTaskText = newText
    }

    open fun insertNewTask() {
        val text = newTaskText
        if (text.isNotBlank()) {
            viewModelScope.launch {
                repository.insert(Task(text = text, isDone = false))
                newTaskText = ""
            }
        }
    }

    open fun updateTaskDoneStatus(taskId: Int, newDoneState: Boolean) {
        if (currentlyEditingTaskId == taskId && !newDoneState) { // If unchecking while editing, save current text
            saveEditedTask(taskId, currentEditText) // or just clear edit state if unchecking means "cancel edit"
        } else if (currentlyEditingTaskId == taskId && newDoneState) { // If checking while editing, save then mark done
            saveEditedTask(taskId, currentEditText) // Save first
        }
        // Always clear editing state for this task if its done status is changing externally.
        if (currentlyEditingTaskId == taskId) {
            currentlyEditingTaskId = null
            currentEditText = ""
        }

        viewModelScope.launch {
            val timestamp = if (newDoneState) System.currentTimeMillis() else null
            repository.updateTaskDoneStatus(taskId, newDoneState /*, timestamp if you use it in DAO/Repo */)
        }
    }

    // --- In-line Editing Logic ---
    open fun startEditingTask(task: TaskUiState) {
        Log.d("ViewModel", "startEditingTask called for ID: ${task.id}. currentText: ${this.currentEditText}, currentEditingID: ${this.currentlyEditingTaskId}")
        // If another task was being edited, save it first
        currentlyEditingTaskId?.let { oldEditingId ->
            if (oldEditingId != task.id) {
                // Consider how you want to handle this. Forcing a save might be best.
                // This call assumes currentEditText holds state for oldEditingId
                // val taskToSave = allTasks.value.find { it.id == oldEditingId }
                // if(taskToSave != null) { // Check if the task still exists
                    // You might need a way to get the *unsaved* edit text for oldEditingId if it's not in currentEditText
                    // For now, this implies currentEditText was for the old task.
                    // A safer model might pass the text to save for the old task.
                    // For simplicity, we are assuming currentEditText is always the one for current task.
                    // So, if we are switching, we need to decide to save or discard.
                    // The original code in your TaskViewModel implied currentEditText was for oldEditingId
                    // but the logic in `onCurrentEditTextChange` implies it's for `currentlyEditingTaskId`.

                    // Let's refine: When starting to edit a *new* task, save the *previous* one.
                    // The `currentEditText` at this point should still hold the value for `oldEditingId`.
                    saveEditedTask(oldEditingId, currentEditText)
            }
        }
        currentlyEditingTaskId = task.id
        currentEditText = task.text // Pre-fill with current text
        Log.d("ViewModel", "startEditingTask DONE. currentEditingID: ${this.currentlyEditingTaskId}, currentText: ${this.currentEditText}")
    }

    open fun onCurrentEditTextChange(newText: String) {
        if (currentlyEditingTaskId != null) { // Only update if a task is actually being edited
            currentEditText = newText
        }
    }

    open fun saveOrDeleteCurrentEditedTask() {
        val editingId = currentlyEditingTaskId
        if (editingId != null) {
            val textToSave = currentEditText
            saveEditedTask(editingId, textToSave)
            // Clear editing state AFTER saving/deleting
            currentlyEditingTaskId = null
            currentEditText = ""
        }
    }

    private fun saveEditedTask(taskId: Int, newText: String) {
        viewModelScope.launch {
            if (newText.isBlank()) {
                // If text is blank after editing, delete the task
                repository.deleteTaskById(taskId)
            } else {
                // Otherwise, update the task
                val originalTaskEntity = repository.getTaskById(taskId)
                if (originalTaskEntity != null) {
                    val updatedTaskEntity = originalTaskEntity.copy(text = newText)
                    repository.update(updatedTaskEntity)
                }
            }
        }
    }

    // Call this if user taps outside or a global "done" action for editing
    fun finishEditing() {
        saveOrDeleteCurrentEditedTask()
    }
}

data class TaskUiState(
    val id: Int,
    val text: String,
    val isDone: Boolean
)

// 3. TaskViewModelFactory now needs to create the repository and pass it
class TaskViewModelFactory(
    private val appContext: Context, // Changed from Application to Context
    private val useInMemoryDb: Boolean = false
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            val db = if (useInMemoryDb) {
                Room.inMemoryDatabaseBuilder(
                    appContext, // Use the provided context directly for Room
                    AppDatabase::class.java
                )
                    .allowMainThreadQueries()
                    .build()
            } else {
                AppDatabase.getDatabase(appContext) // Assumes getDatabase can also take a generic Context
                // and get applicationContext itself if needed for persistent DB.
            }
            val taskDao = db.taskDao()
            val repository = TaskRepository(taskDao)

            // Now, TaskViewModel needs an Application instance.
            // For previews, this is where it gets tricky if AndroidViewModel is strict.
            // For a running app, appContext IS an Application.
            // For previews, appContext is layout lib's context.
            val applicationForViewModel = appContext.applicationContext as? Application
                ?: Application() // Fallback to new Application() for preview, hoping it's enough
            // for what AndroidViewModel might do in a preview context
            // OR, consider if TaskViewModel REALLY needs to be an AndroidViewModel for previews.

            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(applicationForViewModel, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}