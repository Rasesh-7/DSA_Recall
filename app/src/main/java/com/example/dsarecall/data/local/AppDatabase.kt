package com.example.dsarecall.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dsarecall.data.local.dao.ProblemDao
import com.example.dsarecall.data.local.entity.ProblemEntity
import com.example.dsarecall.data.local.entity.RecallAttemptEntity
import com.example.dsarecall.data.local.entity.SheetMembershipEntity

@Database(
    entities = [ProblemEntity::class, SheetMembershipEntity::class, RecallAttemptEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun problemDao(): ProblemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dsa_recall_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
