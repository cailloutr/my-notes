package com.example.mynotes.database.dao

import androidx.room.*
import com.example.mynotes.database.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM note WHERE is_trash != 1")
    fun getAllSavedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE is_trash = 1")
    fun getAllTrashNotes(): Flow<List<Note>>

    @Query("DELETE FROM note WHERE is_trash = 1")
    suspend fun deleteTrash()

    @Query("UPDATE Note SET is_trash = 0 WHERE is_trash = 1")
    suspend fun restoreAllItems()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(vararg note: Note)

    @Delete
    suspend fun delete(vararg note: Note)

}