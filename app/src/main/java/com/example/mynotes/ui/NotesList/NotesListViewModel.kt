package com.example.mynotes.ui.NotesList

import androidx.lifecycle.*
import com.example.mynotes.database.dao.NoteDao
import com.example.mynotes.database.model.Note
import kotlinx.coroutines.flow.Flow

class NotesListViewModel(
    private val notesDao: NoteDao
) : ViewModel() {

    private val _notesList = MutableLiveData<List<Note>>()
    val notesList: LiveData<List<Note>> = _notesList

    init {
        getAllNotes()
    }

    fun getAllNotes(): Flow<List<Note>> = notesDao.getAllNotes()

}

class NotesListViewModelFactory(
    private val notesDao: NoteDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesListViewModel(notesDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}