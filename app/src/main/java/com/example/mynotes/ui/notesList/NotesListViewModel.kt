package com.example.mynotes.ui.notesList

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mynotes.MyNotesApplication
import com.example.mynotes.database.AppDatabase
import com.example.mynotes.database.model.Note
import com.example.mynotes.database.repository.NotesRepository
import com.example.mynotes.util.DateUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NotesListViewModel(
    private val application: MyNotesApplication,
) : ViewModel() {

    private val repository = NotesRepository(AppDatabase.getDatabase(application))

    private val _notesList = repository.getAllNotes()
    val notesList: LiveData<List<Note>> = _notesList

    init {
        getAllNotes()
    }

    fun getAllNotes(): Flow<List<Note>> = application.database.notesDao().getAllNotes()

    fun saveNote(title: String = "", description: String = "") {

        val modifiedDate = DateUtil.getFormattedDate()

        val note = Note(
            title = title,
            description = description,
            modifiedDate = modifiedDate
        )

        viewModelScope.launch {
            repository.insert(note)
        }
    }


}

class NotesListViewModelFactory(
    private val application: MyNotesApplication,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesListViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}