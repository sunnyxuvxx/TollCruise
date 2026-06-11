package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_tools")
data class FavoriteTool(
    @PrimaryKey val toolId: String,
    val isFavorite: Boolean = true
)
