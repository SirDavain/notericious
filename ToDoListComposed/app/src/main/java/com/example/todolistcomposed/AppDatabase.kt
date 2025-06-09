package com.example.todolistcomposed

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// This class will be annotated with @Database
// and will serve as the main access point to your persisted data.

@Database(entities = [Task::class], version = 1, exportSchema = false) // Increment version on schema changes
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database" // Name of your database file
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // .fallbackToDestructiveMigration() // Use with caution during development
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}