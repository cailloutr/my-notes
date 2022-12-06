package com.example.mynotes.database.migrations

import androidx.room.migration.Migration

class MyNotesMigrations {

    companion object {
        private val MIGRATION_11_12 = Migration(11, 12) {
            it.execSQL(""" 
                ALTER TABLE Note ADD color INTEGER
            """.trimIndent())
        }

        val ALL_MIGRATIONS = arrayOf(MIGRATION_11_12)
    }
}