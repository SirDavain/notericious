package com.example.todolistcomposed

import kotlinx.coroutines.flow.Flow

// A Repository class abstracts data sources
// (network, cache, database) from the rest of the app,
// particularly ViewModels.

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun insert(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun getTaskById(taskId: Int): Task? {
        return taskDao.getTaskById(taskId)
    }

    suspend fun update(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTaskById(taskId: Int) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun updateTaskDoneStatus(taskId: Int, isDone: Boolean) {
        taskDao.updateTaskDoneStatus(taskId, isDone)
    }
}