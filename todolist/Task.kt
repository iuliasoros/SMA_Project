package com.example.myapplicationtodolist

import java.util.UUID

data class TodoItem(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)
