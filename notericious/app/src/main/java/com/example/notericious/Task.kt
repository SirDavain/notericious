package com.example.todolistcomposed

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var title: String = "",
    var content: String = "",
    val isDone: Boolean = false,
    var completedOrReopenedTimestamp: Long? = null,
    val isNote: Boolean = false
)