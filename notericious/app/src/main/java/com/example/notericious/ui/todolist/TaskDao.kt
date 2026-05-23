package com.example.notericious

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
        WHERE parentId IS NULL
        ORDER BY
            isDone ASC,
            CASE
                WHEN isDone = 0 THEN completedOrReopenedTimestamp
                ELSE NULL
            END ASC,
            CASE
                WHEN isDone = 1 THEN completedOrReopenedTimestamp
                ELSE NULL
            END DESC,
            id ASC
    """)
    fun getTopLevelTasks(): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks
        WHERE parentId = :parentId
        ORDER BY
            isDone ASC,
            CASE
                WHEN isDone = 0 THEN completedOrReopenedTimestamp
                ELSE NULL
            END ASC,
            CASE
                WHEN isDone = 1 THEN completedOrReopenedTimestamp
                ELSE NULL
            END DESC,
            id ASC
    """)
    fun getTasksByParentId(parentId: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    // Keep this or adapt it if you directly update isDone
    @Query("UPDATE tasks SET isDone = :isDone, completedOrReopenedTimestamp = :timestamp WHERE id = :taskId")
    suspend fun updateTaskDoneStatusAndTimestamp(taskId: Int, isDone: Boolean, timestamp: Long)

    /*@Query()
    suspend fun getAllNotes()*/
}