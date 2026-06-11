package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.FavoriteTool
import com.example.data.model.HistoryItem
import com.example.data.model.UserBilling

@Database(entities = [HistoryItem::class, FavoriteTool::class, UserBilling::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun toolDao(): ToolDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "toolcrux_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
