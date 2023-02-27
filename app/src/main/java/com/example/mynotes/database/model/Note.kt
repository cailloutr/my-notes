package com.example.mynotes.database.model

import androidx.annotation.ColorRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Note(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    var title: String? = "",
    var description: String? = "",
    @ColumnInfo(name = "modified_date") var modifiedDate: String?,
    @ColumnInfo(name = "is_trash") var isTrash: Boolean? = false,
    @ColorRes var color: Int? = null,
    @ColumnInfo(name = "image_url") var imageUrl: String? = null,
    @ColumnInfo(name = "has_image") var hasImage: Boolean = false
)
