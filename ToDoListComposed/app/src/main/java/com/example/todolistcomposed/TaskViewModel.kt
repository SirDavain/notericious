package com.example.todolistcomposed

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

open class TaskViewModel(
    application: Application, // AndroidViewModel requires Application
    private val repository: TaskRepository
) : AndroidViewModel(application) {

    // TaskUiState should remain the same if it doesn't need the timestamp for UI display directly
    // If you wanted to display the timestamp, you'd add it to TaskUiState
    open val allTasks: StateFlow<List<TaskUiState>> = repository.allTasks.map { tasks ->
        // tasks is List<Task> from DAO (which includes completedOrReopenedTimestamp)
        tasks.map { task ->
            // Map to TaskUiState. The timestamp is used for sorting in DAO,
            // not necessarily needed in TaskUiState unless you display it.
            TaskUiState(task.id, task.text, task.isDone)
        }
    }.stateIn(
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
                    text = text,
                    isDone = false,
                    completedOrReopenedTimestamp = currentTime
                )
                repository.insert(taskToInsert) // Assuming repository.insert takes a Task object
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
            val originalTaskEntity = repository.getTaskById(taskId) // Assuming repository has getTaskById
            if (originalTaskEntity != null) {
                val updatedTaskEntity = originalTaskEntity.copy(
                    isDone = newDoneState,
                    completedOrReopenedTimestamp = currentTime
                )
                repository.update(updatedTaskEntity)
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
                repository.deleteTaskById(taskId)
            } else {
                val originalTaskEntity = repository.getTaskById(taskId)
                if (originalTaskEntity != null) {
                    // Only update the text. Keep existing isDone and completedOrReopenedTimestamp
                    val updatedTaskEntity = originalTaskEntity.copy(text = trimmedText)
                    repository.update(updatedTaskEntity)
                }
            }
        }
    }

    fun finishEditing() {
        saveOrDeleteCurrentEditedTask()
    }
}

// TaskUiState doesn't necessarily need the timestamp unless you display it.
// The sorting happens at the DAO/database level.
data class TaskUiState(
    val id: Int,
    val text: String,
    val isDone: Boolean
)

// Factory remains mostly the same, ensure AppDatabase is correctly versioned and has migrations
class TaskViewModelFactory(
    private val application: Application, // Changed to Application
    private val useInMemoryDb: Boolean = false
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            val db = if (useInMemoryDb) {
                Room.inMemoryDatabaseBuilder(
                    application.applicationContext, // Use application context
                    AppDatabase::class.java // Ensure this is your updated AppDatabase class
                )
                    .allowMainThreadQueries() // Only for testing/previews if absolutely needed
                    .build()
            } else {
                // Ensure AppDatabase.getDatabase is using the application context
                // and has the migration added for the new timestamp column.
                AppDatabase.getDatabase(application.applicationContext)
            }
            val taskDao = db.taskDao()
            val repository = TaskRepository(taskDao) // Ensure TaskRepository uses this DAO

            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}