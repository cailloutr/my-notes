package com.example.mynotes.database.migrations

import androidx.room.migration.Migration

class MyNotesMigrations {

    companion object {
        private val MIGRATION_11_12 = Migration(11, 12) {
            it.execSQL(""" 
                ALTER TABLE Note ADD color INTEGER
            """.trimIndent())
        }

        private val MIGRATION_12_13 = Migration(12, 13) {
            it.execSQL(""" 
                ALTER TABLE Note ADD image_url TEXT
            """.trimIndent())
        }

        private val MIGRATION_13_14 = Migration(13, 14) {
            it.execSQL(""" 
                ALTER TABLE Note ADD has_image INTEGER DEFAULT 0 NOT NULL
            """.trimIndent())
        }

        private val MIGRATION_14_13 = Migration(14, 13) {
            it.execSQL(""" 
                ALTER TABLE Note DROP has_image
            """.trimIndent())
        }

        val ALL_MIGRATIONS = arrayOf(MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_13)
    }
}