package com.example.mynotes.database.migrations

import androidx.room.migration.Migration
import java.util.UUID

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

        private val MIGRATION_14_15 = Migration(14, 15) {
            val currentTable = "Note"
            val newTable = "NewTable"

            //Create a copy of the current table
            it.execSQL(""" 
                CREATE TABLE IF NOT EXISTS $newTable (
                `id` TEXT NOT NULL, 
                `title` TEXT, 
                `description` TEXT, 
                `modified_data` TEXT, 
                `is_trash` INTEGER, 
                `position` INTEGER, 
                `color` INTEGER, 
                `image_url` TEXT, 
                `has_image` INTEGER NOT NULL, 
                PRIMARY KEY(`id`)
                )
            """.trimIndent())

            // Copy the data from old table to new table
            it.execSQL("""
                INSERT INTO $newTable (
                `id`, 
                `title`, 
                `description`, 
                `modified_data`, 
                `is_trash`, 
                `position`, 
                `color`, 
                `image_url`, 
                `has_image`
                )   
                SELECT 
                `id`, 
                `title`, 
                `description`, 
                `modified_data`, 
                `is_trash`, 
                `position`, 
                `color`, 
                `image_url`, 
                `has_image`
                FROM $currentTable
            """.trimIndent())

            // Set new ids for every row
            val cursor = it.query("SELECT * FROM $newTable")
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
                it.execSQL("""
                    UPDATE $newTable SET `id` = "${UUID.randomUUID()}"
                    WHERE `id` = $id
                """.trimIndent())
            }

            // Drop the current table
            it.execSQL("""
                DROP TABLE $currentTable
            """.trimIndent())

            // Rename the new table to old table's name
            it.execSQL("""
                ALTER TABLE $newTable RENAME TO $currentTable
            """.trimIndent())
        }

        val ALL_MIGRATIONS = arrayOf(
            MIGRATION_11_12,
            MIGRATION_12_13,
            MIGRATION_13_14,
            MIGRATION_14_13,
            MIGRATION_14_15
        )
    }
}