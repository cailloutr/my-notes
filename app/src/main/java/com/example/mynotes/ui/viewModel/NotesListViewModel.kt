package com.example.mynotes.ui.viewModel

import androidx.lifecycle.*
import com.example.mynotes.MyNotesApplication
import com.example.mynotes.database.AppDatabase
import com.example.mynotes.database.model.Note
import com.example.mynotes.database.repository.NotesRepository
import com.example.mynotes.ui.enums.FragmentMode
import com.example.mynotes.ui.enums.LayoutMode
import com.example.mynotes.util.DateUtil
import kotlinx.coroutines.launch

class NotesListViewModel(
    application: MyNotesApplication,
) : ViewModel() {

    private val TAG: String = "NoteListViewModel"
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

    var layoutMode: LayoutMode = LayoutMode.STAGGERED_GRID_LAYOUT


    private fun getAllNotes() = repository.getAllSavedNotes()

    private fun getTrash() = repository.getAllTrashNotes()

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
    }

/*    fun swipePositions(initPosition: Long, finalPosition: Long) {
        viewModelScope.launch {
            val notePosition = notesList.value?.get(initPosition.toInt())?.position
            val targetPosition = notesList.value?.get(finalPosition.toInt())?.position
            repository.swipePositions(notePosition!!, targetPosition!!)
        }
    }

    fun swipePositions(oldPosition: Long, newPosition: Long) {
        viewModelScope.launch {
            repository.updatePosition(oldPosition.toInt(), newPosition.toInt())
            repository.updatePosition(newPosition.toInt(), oldPosition.toInt())
        }
    }

    fun updateListPositions(item: Long, target: Long) {

        val itemPosition = notesList.value?.get(item.toInt())?.position
        val targetPosition = notesList.value?.get(target.toInt())?.position

        if (targetPosition?.compareTo(itemPosition!!)!! > 0) {
            viewModelScope.launch {
                repository.moveUpItem(itemPosition!!, targetPosition)
            }
        } else if (targetPosition < itemPosition!!) {
            viewModelScope.launch {
                repository.moveDownItem(itemPosition, targetPosition)
            }
        }
    }*/


    fun clearTrash() {
        viewModelScope.launch {
            repository.clearTrash()
        }
    }

    fun restoreAllNotesFromTrash() {
        viewModelScope.launch {
            repository.restoreAllNotesFromTrash()
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

    fun deleteSelectedNotes(listOfItemToDelete: List<Note>) {
        viewModelScope.launch {
            repository.delete(*listOfItemToDelete.toTypedArray())
        }
    }

    fun moveNoteToTrash(): Boolean {
        if (note.value?.id == null) {
            return false
        }

        _note.value?.isTrash = true
        return true
    }

    fun moveSelectedItemsToTrash(listOfItem: List<Note>) {
        val list = listOfItem.toMutableList()
        list.forEach {
            it.isTrash = true
        }

        viewModelScope.launch {
            repository.update(*list.toTypedArray())
        }
    }

    fun retrieveNoteFromTrash() {
        _note.value?.isTrash = false
    }

    fun updateViewModelNote(title: String = "", description: String) {
//        if (_note.value?.id == null) {
//            createEmptyNote()
//        }

        _note.value?.title = title
        _note.value?.description = description
        _note.value?.modifiedDate = DateUtil.getFormattedDate()
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

    fun setNoteColor(color: Int) {
        _note.value?.color = color
    }

    fun createEmptyNote() {
        _note.value = Note(
            title = "",
            description = "",
            modifiedDate = null,
            isTrash = false
        )
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