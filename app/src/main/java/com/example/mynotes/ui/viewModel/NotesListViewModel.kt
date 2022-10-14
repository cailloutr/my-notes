package com.example.mynotes.ui.viewModel

import androidx.lifecycle.*
import com.example.mynotes.MyNotesApplication
import com.example.mynotes.database.AppDatabase
import com.example.mynotes.database.model.Note
import com.example.mynotes.database.repository.NotesRepository
import com.example.mynotes.util.DateUtil
import kotlinx.coroutines.launch

class NotesListViewModel(
    application: MyNotesApplication,
) : ViewModel() {

    private val repository = NotesRepository(AppDatabase.getDatabase(application))

    private val _notesList = getAllNotes()
    val notesList: LiveData<List<Note>> = _notesList

    private var _newNoteDescription = MutableLiveData<String?>("Inicial Value")
    val newNoteDescription: LiveData<String?> = _newNoteDescription

    private fun getAllNotes() = repository.getAllNotes()

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

    fun setNewNoteDescription(description: String?) {
        _newNoteDescription.value = description
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