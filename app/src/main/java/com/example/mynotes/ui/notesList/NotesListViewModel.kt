package com.example.mynotes.ui.notesList

import androidx.lifecycle.*
import com.example.mynotes.MyNotesApplication
import com.example.mynotes.database.AppDatabase
import com.example.mynotes.database.dao.NoteDao
import com.example.mynotes.database.model.Note
import com.example.mynotes.database.repository.NotesRepository
import kotlinx.coroutines.flow.Flow

class NotesListViewModel(
    private val application: MyNotesApplication
) : ViewModel() {

    private val repository = NotesRepository(AppDatabase.getDatabase(application))

    private val _notesList = repository.getAllNotes()
    val notesList: LiveData<List<Note>> = _notesList

    init {
        getAllNotes()
    }

    fun getAllNotes(): Flow<List<Note>> = application.database.notesDao().getAllNotes()
    fun getAllNotesAsLiveData(): LiveData<List<Note>> = repository.getAllNotes()

}

class NotesListViewModelFactory(
    private val application: MyNotesApplication
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesListViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}