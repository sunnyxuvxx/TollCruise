package com.example.data.database

import androidx.room.*
import com.example.data.model.FavoriteTool
import com.example.data.model.HistoryItem
import com.example.data.model.UserBilling
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolDao {
    @Query("SELECT * FROM history_items ORDER BY timestamp DESC LIMIT 50")
    fun getAllHistory(): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryItem)

    @Query("DELETE FROM history_items")
    suspend fun clearHistory()

    @Query("SELECT * FROM favorite_tools")
    fun getFavoriteTools(): Flow<List<FavoriteTool>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(fav: FavoriteTool)

    @Query("DELETE FROM favorite_tools WHERE toolId = :toolId")
    suspend fun deleteFavorite(toolId: String)

    @Query("SELECT * FROM user_billing WHERE id = 1 LIMIT 1")
    fun getUserBilling(): Flow<UserBilling?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserBilling(billing: UserBilling)
}
