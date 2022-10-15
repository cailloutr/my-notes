package com.example.mynotes.ui.viewModel

import androidx.lifecycle.*
import com.example.mynotes.MyNotesApplication
import com.example.mynotes.database.AppDatabase
import com.example.mynotes.database.model.Note
import com.example.mynotes.database.repository.NotesRepository
import com.example.mynotes.util.DateUtil
import kotlinx.coroutines.launch
import com.example.mynotes.ui.enums.FragmentMode

class NotesListViewModel(
    application: MyNotesApplication,
) : ViewModel() {

    private val repository = NotesRepository(AppDatabase.getDatabase(application))

    private val _notesList = getAllNotes()
    val notesList: LiveData<List<Note>> = _notesList

    private var _fragmentMode: MutableLiveData<FragmentMode> =
        MutableLiveData(FragmentMode.FRAGMENT_NEW)
    val fragmentMode: LiveData<FragmentMode> = _fragmentMode

    private val _note = MutableLiveData<Note?>(null)
    val note: LiveData<Note?> = _note

    private val _noteTrash = MutableLiveData<Note?>(null)
    val noteTrash: LiveData<Note?> = _noteTrash


    private fun getAllNotes() = repository.getAllNotes()

    fun saveNote() {

        if (note.value?.id == null) {
            viewModelScope.launch {
                note.value?.let { repository.insert(it) }
            }
        } else {
            viewModelScope.launch {
                note.value?.let { repository.update(it) }
            }
        }
        cleatNote()
    }

    fun undoDeleteNote(){
        viewModelScope.launch {
            note.value?.let { repository.insert(it) }
        }
        cleatNote()
    }

    // Return false if Note don't exist
    fun deleteNote(): Boolean {
        if (note.value?.id == null) {
            cleatNote()
            return false
        }

        viewModelScope.launch {
            note.value?.let { repository.delete(it) }
        }
        cleatNote()
        return true
    }

    fun moveNoteToTrash(): Boolean {
        if (note.value?.id == null) {
            return false
        }

        _noteTrash.value = note.value
        return true
    }

    fun retrieveNoteFromTrash() {
        _note.value = noteTrash.value
        clearTrash()
    }

    fun updateViewModelNote(title: String = "", description: String) {
        if (note.value?.id != null) {
            _note.value?.title = title
            _note.value?.description = description
            _note.value?.modifiedDate = DateUtil.getFormattedDate()
        } else {
            val note = Note(
                title = title,
                description = description,
                modifiedDate = DateUtil.getFormattedDate()
            )
            _note.value = note
        }
    }

    fun loadNote(note: Note) {
        _note.value = note
    }

    private fun cleatNote() {
        _note.value = null
    }

    private fun clearTrash() {
        _noteTrash.value = null
    }

    fun setFragmentMode(mode: FragmentMode) {
        _fragmentMode.value = mode
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