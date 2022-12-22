package com.example.mynotes.database.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.mynotes.database.AppDatabase
import com.example.mynotes.database.model.Note

class NotesRepository(private val appDatabase: AppDatabase) {

    fun getAllSavedNotes(): LiveData<List<Note>> {
        return appDatabase.notesDao().getAllSavedNotes().asLiveData()
    }

    fun getAllTrashNotes(): LiveData<List<Note>> {
        return appDatabase.notesDao().getAllTrashNotes().asLiveData()
    }

    suspend fun clearTrash() {
        appDatabase.notesDao().deleteTrash()
    }

    suspend fun restoreAllNotesFromTrash() {
        appDatabase.notesDao().restoreAllItems()
    }

    suspend fun insert(note: Note) {
        appDatabase.notesDao().insert(note)
        appDatabase.notesDao().insertPosition()
    }

    suspend fun update(note: Note) {
        appDatabase.notesDao().update(note)
    }

    suspend fun delete(vararg note: Note) {
        appDatabase.notesDao().delete(*note)
    }
}