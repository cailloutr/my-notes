package com.example.mynotes.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mynotes.database.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM note WHERE is_trash != 1 ORDER BY id")
    fun getAllSavedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE is_trash = 1 ORDER BY id")
    fun getAllTrashNotes(): Flow<List<Note>>

    @Query("DELETE FROM note WHERE is_trash = 1")
    suspend fun deleteTrash()

    @Query("UPDATE Note SET is_trash = 0 WHERE is_trash = 1")
    suspend fun restoreAllItems()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

}