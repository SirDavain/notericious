package com.example.notericious

data class Note(
    val id: Int? = null, // Nullable because new notes don't have an ID yet
    val user_id: String,
    val title: String,
    val content: String
)