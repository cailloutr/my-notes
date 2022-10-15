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


//    private var _noteId = MutableLiveData<Int>()
//    val noteId: LiveData<Int> = _noteId
//
//    private var _noteTitle = MutableLiveData<String?>()
//    val noteTitle: LiveData<String?> = _noteTitle
//
//    private var _noteDescription = MutableLiveData<String?>()
//    val noteDescription: LiveData<String?> = _noteDescription
//
//    private var _noteDate = MutableLiveData<String?>()
//    val noteDate: LiveData<String?> = _noteDate

    // TODO: update note instead of crating a new

    private fun getAllNotes() = repository.getAllNotes()

//    fun saveNote(title: String = "", description: String = "") {
//
//        val modifiedDate = DateUtil.getFormattedDate()
//
//        val note = Note(
//            title = title,
//            description = description,
//            modifiedDate = modifiedDate
//        )
//
//        viewModelScope.launch {
//            repository.insert(note)
//        }
//    }

    fun updateNote() {
        viewModelScope.launch {
            note.value?.let { repository.update(it) }
        }

        cleatNote()
    }

    fun saveNote() {

        if (_note.value?.id == null) {
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

    fun createNote(description: String?) {
        val note = Note(
            title = "",
            description = description,
            modifiedDate = DateUtil.getFormattedDate()
        )
        _note.value = note
    }

    fun updateViewModelNote(title: String, description: String) {
        if (note.value?.id != null) {
            _note.value?.title = title
            _note.value?.description = description
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

    fun cleatNote() {
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