package com.example.mynotes.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    var title: String? = "",
    var description: String? = "",
    @ColumnInfo(name = "modified_data") var modifiedDate: String?
) {
}