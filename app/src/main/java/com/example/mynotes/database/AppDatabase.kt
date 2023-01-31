package com.example.mynotes.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mynotes.database.dao.NoteDao
import com.example.mynotes.database.model.Note

@Database(entities = [Note::class], version = 12, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notesDao(): NoteDao

/*    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "app_database")
                    .addMigrations(*MyNotesMigrations.ALL_MIGRATIONS)
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }*/
}