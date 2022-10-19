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


    suspend fun insert(note: Note) {
        appDatabase.notesDao().insert(note)
    }

    suspend fun update(note: Note) {
        appDatabase.notesDao().update(note)
    }

    suspend fun delete(note: Note) {
        appDatabase.notesDao().delete(note)
    }
}