package com.example.mynotes.ui.viewModel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynotes.model.Note
import com.example.mynotes.model.UserPreferences
import com.example.mynotes.repository.InternalStorageRepository
import com.example.mynotes.repository.NotesRepository
import com.example.mynotes.repository.UserPreferencesRepository
import com.example.mynotes.ui.enums.LayoutMode
import com.example.mynotes.util.DateUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File

class NotesListViewModel(
    private val repository: NotesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val internalStorageRepository: InternalStorageRepository
) : ViewModel() {

//    private val TAG: String = "NoteListViewModel"

    private val _notesList = getAllNotes()
    val notesList: LiveData<List<Note>> = _notesList

    private val _trashList = getTrash()
    val trashList: LiveData<List<Note>> = _trashList

    private val _note = MutableLiveData<Note?>(null)
    val note: LiveData<Note?> = _note

    private val _hasImage = MutableLiveData(false)
    val hasImage: LiveData<Boolean> = _hasImage

    private var _layoutMode: LayoutMode = LayoutMode.STAGGERED_GRID_LAYOUT
    val layoutMode get() = _layoutMode

    val userPreferencesFlow: Flow<UserPreferences> = userPreferencesRepository.userPreferencesFlow

    private var _hasPreferencesChanged: Boolean = false
    val hasPreferencesChanged get() = _hasPreferencesChanged

    private var _currentPhotoPath: String? = null
    val currentPhotoPath: String? get() = _currentPhotoPath

    var uri: Uri? = null

    private fun getAllNotes() = repository.getAllSavedNotes()

    private fun getTrash() = repository.getAllTrashNotes()

    fun saveNote() {
        viewModelScope.launch {
            note.value?.let { repository.insert(it) }
        }
    }

    fun saveNewNote(
        title: String,
        description: String,
        bitmap: Bitmap?,
        context: Context
    ) {
        val imagePath: String? = if (currentPhotoPath.isNullOrEmpty()) {
            saveImageFromImageView(bitmap, context)
        } else {
            saveImageFromCache(context)
        }

        updateViewModelNote(
            title = title,
            description = description,
            imagePath = imagePath ?: "",
            hasImage = hasImage.value
        )

        setCurrentPhotoPath(null)
        saveNote()
    }

    private fun saveImageFromCache(context: Context): String? {
        val source = currentPhotoPath?.let { File(it) }
        val destination = getImagePath(context)?.let { File(it) }
        copyFiles(source, destination)
        return destination?.absolutePath
    }

    private fun saveImageFromImageView(bitmap: Bitmap?, context: Context): String {
        return if (hasImage.value == true) {
            saveImageInAppSpecificAlbumStorageDir(
                bitmap,
                getImagePath(context).toString()
            )
        } else {
            deleteImageInAppSpecificAlbumStorageDir(context)
            ""
        }
    }

    fun createTempFile(context: Context, id: String): File {
        return internalStorageRepository.createTempImageFile(context, id).apply {
            setCurrentPhotoPath(absolutePath)
        }
    }

    private fun copyFiles(source: File?, destination: File?) =
        internalStorageRepository.copyFile(source, destination)

    private fun deleteImageInAppSpecificAlbumStorageDir(context: Context) {
        note.value?.id?.let {
            internalStorageRepository.deleteImageInAppSpecificAlbumStorageDir(
                context, it
            )
        }
    }

    private fun saveImageInAppSpecificAlbumStorageDir(
        bitmap: Bitmap?,
        path: String
    ): String {
        return internalStorageRepository.saveImageInAppSpecificAlbumStorageDir(
            bitmap, path
        )
    }

/*    fun getAppSpecificAlbumStorageDir(context: Context) =
        internalStorageRepository.getAppSpecificAlbumStorageDir(context)*/

    private fun getImagePath(context: Context): String? =
        note.value?.id?.let { internalStorageRepository.getImagePath(context, it) }


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

    private fun setCurrentPhotoPath(path: String?) {
        _currentPhotoPath = path
    }

    fun noteIsTrash(): Boolean = note.value?.isTrash ?: false

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
