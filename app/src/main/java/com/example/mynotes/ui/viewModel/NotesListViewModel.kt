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

    private val _trashList = getTrash()
    val trashList: LiveData<List<Note>> = _trashList

    private var _fragmentMode: MutableLiveData<FragmentMode> =
        MutableLiveData(FragmentMode.FRAGMENT_NEW)
    val fragmentMode: LiveData<FragmentMode> = _fragmentMode

    private val _note = MutableLiveData<Note?>(null)
    val note: LiveData<Note?> = _note


    private fun getAllNotes() = repository.getAllSavedNotes()

    private fun getTrash() = repository.getAllTrashNotes()

    fun saveNote() {

        if (note.value?.id == null) {
            viewModelScope.launch() {
                note.value?.let { repository.insert(it) }
            }
        } else {
            viewModelScope.launch() {
                note.value?.let { repository.update(it) }
            }
        }

    }

    fun clearTrash() {
        viewModelScope.launch {
            repository.clearTrash()
        }
    }

    // Return false if Note don't exist
    fun deleteNote(): Boolean {
        if (note.value?.isTrash == true) {
            viewModelScope.launch {
                note.value?.let {
                    repository.delete(it)
                }
            }
        }

        clearNote()
        return true
    }

    fun moveNoteToTrash(): Boolean {
        if (note.value?.id == null) {
            return false
        }

        _note.value?.isTrash = true
        return true
    }

    fun retrieveNoteFromTrash() {
        _note.value?.isTrash = false
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
                modifiedDate = DateUtil.getFormattedDate(),
                isTrash = false
            )
            _note.value = note
        }
    }

    fun loadNote(note: Note) {
        _note.value = note
    }

    fun clearNote() {
        _note.value = null
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