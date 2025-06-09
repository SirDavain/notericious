package com.example.todolistcomposed

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// The DAO is an interface that defines
// how you interact with your database
// (insert, query, update, delete tasks).

// In TaskDao.kt
@Dao
interface TaskDao {
    // Modify getAllTasks to sort by isDone, then by id
    @Query("SELECT * FROM tasks ORDER BY isDone ASC") // ASC for isDone means false (0) comes before true (1)

    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task) // We'll use this for updating isDone

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    @Query("UPDATE tasks SET isDone = :isDone WHERE id = :taskId") // This query is fine
    suspend fun updateTaskDoneStatus(taskId: Int, isDone: Boolean)
}