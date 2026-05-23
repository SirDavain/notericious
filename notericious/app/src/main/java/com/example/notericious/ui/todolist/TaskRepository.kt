package com.example.notericious

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getTopLevelTasks()

    fun getTasksByParentId(parentId: Int): Flow<List<Task>> = taskDao.getTasksByParentId(parentId)

    /*suspend fun getAllNotes() {
        taskDao.getAllNotes()
    }*/

    suspend fun insert(task: Task): Long {
        return taskDao.insertTask(task)
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

    suspend fun updateTaskDoneStatus(taskId: Int, isDone: Boolean, timestamp: Long) {
        taskDao.updateTaskDoneStatusAndTimestamp(taskId, isDone, timestamp)
    }
}