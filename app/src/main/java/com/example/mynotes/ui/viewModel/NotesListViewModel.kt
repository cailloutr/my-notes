package com.example.mynotes.ui.viewModel

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynotes.database.model.Note
import com.example.mynotes.database.model.UserPreferences
import com.example.mynotes.database.repository.NotesRepository
import com.example.mynotes.database.repository.UserPreferencesRepository
import com.example.mynotes.ui.enums.FragmentMode
import com.example.mynotes.ui.enums.LayoutMode
import com.example.mynotes.util.DateUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class NotesListViewModel(
    private val repository: NotesRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val TAG: String = "NoteListViewModel"
    private val ALBUM_NAME = "notes"

    private val _notesList = getAllNotes()
    val notesList: LiveData<List<Note>> = _notesList

    private val _trashList = getTrash()
    val trashList: LiveData<List<Note>> = _trashList

    private var _fragmentMode: MutableLiveData<FragmentMode> =
        MutableLiveData(FragmentMode.FRAGMENT_NEW)
    val fragmentMode: LiveData<FragmentMode> = _fragmentMode

    private val _note = MutableLiveData<Note?>(null)
    val note: LiveData<Note?> = _note

    private val _hasImage = MutableLiveData(false)
    val hasImage: LiveData<Boolean> = _hasImage

    private var _layoutMode: LayoutMode = LayoutMode.STAGGERED_GRID_LAYOUT
    val layoutMode get() = _layoutMode

    val userPreferencesFlow: Flow<UserPreferences> = userPreferencesRepository.userPreferencesFlow

    private var _hasPreferencesChanged: Boolean = false
    val hasPreferencesChanged get() = _hasPreferencesChanged

    private fun getAllNotes() = repository.getAllSavedNotes()

    private fun getTrash() = repository.getAllTrashNotes()

    fun saveNote() {
        viewModelScope.launch {
            note.value?.let { repository.insert(it) }
        }
    }

    private fun getAppSpecificAlbumStorageDir(context: Context): File {
        // Get the pictures directory that's inside the app-specific directory on
        // external storage.
        val file = File(
            context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES
            ), ALBUM_NAME
        )
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created")
        }
        return file
    }

    fun deleteImageInAppSpecificAlbumStorageDir(context: Context) {
        val path = getImagePath(context)
        val file = File(path)

        if (file.exists()) {
            try {
                val fileDeleted = file.delete()
                Log.d(TAG, "deleteImageInAppSpecificAlbumStorageDir: $fileDeleted")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveImageInAppSpecificAlbumStorageDir(bitmap: Bitmap?, context: Context): String {
        var baos: OutputStream? = null
        val path = getImagePath(context)
        val file = File(path)
        try {
            baos = FileOutputStream(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        try {
            baos?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            baos?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return path
    }

    private fun getImagePath(context: Context) =
        "${getAppSpecificAlbumStorageDir(context)}/${note.value?.id}.jpg"


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

    fun deleteNote() {
        if (note.value?.isTrash == true) {
            viewModelScope.launch {
                note.value?.let {
                    repository.delete(it)
                }
            }
        }
        clearNote()
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

    fun restoreSelectedNotes(listOfSelectedItems: List<Note>) {
        for (note in listOfSelectedItems) {
            note.isTrash = false
        }
        viewModelScope.launch {
            repository.update(*listOfSelectedItems.toTypedArray())
        }
    }

    fun updateViewModelNote(
        title: String = "",
        description: String,
        imagePath: String = "",
        hasImage: Boolean? = false
    ) {
        _note.value?.title = title
        _note.value?.description = description
        _note.value?.modifiedDate = DateUtil.getFormattedDate()
        _note.value?.imageUrl = imagePath
        hasImage?.let {
            _note.value?.hasImage = hasImage
        }
    }

    fun updateViewModelNoteHasImage(hasImage: Boolean) {
        setHasImageValue(hasImage)
    }

    private fun setHasImageValue(hasImage: Boolean) {
        _hasImage.value = hasImage
    }

    fun loadNote(note: Note) {
        _note.value = note
        setHasImageValue(note.hasImage)
    }

    private fun clearNote() {
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
        updateViewModelNoteHasImage(_note.value!!.hasImage)
    }

    fun updateLayoutMode() {
        viewModelScope.launch {
            userPreferencesRepository.updateLayoutMode(layoutMode)
        }
    }

    fun hasPreferencesChanged(changed: Boolean) {
        _hasPreferencesChanged = changed
    }

    fun setLayoutMode(mode: LayoutMode) {
        _layoutMode = mode
    }

}

/*
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
}*/
