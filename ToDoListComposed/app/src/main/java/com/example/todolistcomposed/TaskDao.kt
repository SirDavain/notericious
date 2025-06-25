package com.example.todolistcomposed

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// The DAO is an interface that defines
// how you interact with your database
// (insert, query, update, delete tasks).

@Dao
interface TaskDao {
    @Query("""
        SELECT * FROM tasks
        ORDER BY
            isDone ASC, -- false (0) before true (1)
            CASE
                WHEN isDone = 0 THEN completedOrReopenedTimestamp -- For undone items, sort by timestamp ASC (older first, newer later)
                ELSE NULL -- This part of CASE won't be used for isDone = 0
            END ASC,
            CASE
                WHEN isDone = 1 THEN completedOrReopenedTimestamp -- For done items, sort by timestamp DESC (newer first)
                ELSE NULL -- This part of CASE won't be used for isDone = 1
            END DESC,
            id ASC -- Fallback to id for items with identical timestamps (unlikely but good for stability)
    """)
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    // Keep this or adapt it if you directly update isDone
    @Query("UPDATE tasks SET isDone = :isDone, completedOrReopenedTimestamp = :timestamp WHERE id = :taskId")
    suspend fun updateTaskDoneStatusAndTimestamp(taskId: Int, isDone: Boolean, timestamp: Long)
}