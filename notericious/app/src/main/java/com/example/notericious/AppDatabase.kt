package com.example.notericious

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// This class will be annotated with @Database
// and will serve as the main access point to your persisted data.

@Database(entities = [Task::class], version = 3, exportSchema = false) // Increment version on schema changes
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add the new column, allowing NULLs initially, or provide a default
                db.execSQL("ALTER TABLE tasks ADD COLUMN completedOrReopenedTimestamp INTEGER")
            }
        }
        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // Name of your database file
                )
                    .addMigrations(MIGRATION_1_2)
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    .fallbackToDestructiveMigration(dropAllTables = true) // Use with caution during development
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}