package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val toolId: String,
    val toolName: String,
    val actionType: String, // e.g. "Format", "Generate", "Encode"
    val timestamp: Long = System.currentTimeMillis(),
    val details: String // e.g. "Formatted 120 chars of JSON", "Generated 5 UUIDs"
)
