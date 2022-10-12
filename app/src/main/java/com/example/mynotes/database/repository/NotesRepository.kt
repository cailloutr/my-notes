package com.example.mynotes.database.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.mynotes.database.AppDatabase
import com.example.mynotes.database.model.Note

class NotesRepository(private val appDatabase: AppDatabase) {

    fun getAllNotes(): LiveData<List<Note>> {
        return appDatabase.notesDao().getAllNotes().asLiveData()
    }
}