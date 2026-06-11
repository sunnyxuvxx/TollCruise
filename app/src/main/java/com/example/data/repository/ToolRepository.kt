package com.example.data.repository

import com.example.data.database.ToolDao
import com.example.data.model.FavoriteTool
import com.example.data.model.HistoryItem
import com.example.data.model.UserBilling
import kotlinx.coroutines.flow.Flow

class ToolRepository(private val toolDao: ToolDao) {
    val allHistory: Flow<List<HistoryItem>> = toolDao.getAllHistory()
    val favoriteTools: Flow<List<FavoriteTool>> = toolDao.getFavoriteTools()
    val userBilling: Flow<UserBilling?> = toolDao.getUserBilling()

    suspend fun insertHistory(item: HistoryItem) {
        toolDao.insertHistory(item)
    }

    suspend fun clearHistory() {
        toolDao.clearHistory()
    }

    suspend fun toggleFavorite(toolId: String, isFav: Boolean) {
        if (isFav) {
            toolDao.insertFavorite(FavoriteTool(toolId, true))
        } else {
            toolDao.deleteFavorite(toolId)
        }
    }

    suspend fun updateUserBilling(billing: UserBilling) {
        toolDao.insertUserBilling(billing)
    }
}
