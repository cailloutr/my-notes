package com.example.mynotes.database.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.mynotes.database.AppDatabase
import com.example.mynotes.database.model.Note

class NotesRepository(private val appDatabase: AppDatabase) {

    fun getAllSavedNotes(): LiveData<List<Note>> {
        return appDatabase.notesDao().getAllSavedNotes().asLiveData()
    }

    fun getAllTrashNotes(): LiveData<List<Note>> {
        return appDatabase.notesDao().getAllTrashNotes().asLiveData()
    }


    suspend fun swipePositions(initPosition: Long, finalPosition: Long) {
        appDatabase.notesDao().swipePositions(initPosition, finalPosition)
    }


//    suspend fun updatePosition(oldPosition: Int, newPosition: Int) {
//        appDatabase.notesDao().updatePosition(oldPosition, newPosition)

        //TODO: move down not working properly
        suspend fun moveDownItem(item: Long, target: Long) {
            with(appDatabase.notesDao()) {
                updatePositionDown1(target)
                updatePositionDown2(item, target)
                updatePositionDown3(item)
            }
        }

        suspend fun moveUpItem(item: Long, target: Long) {
            with(appDatabase.notesDao()) {
                updatePositionUp1(target)
                updatePositionUp2(item, target)
                updatePositionUp3(item)
            }
        }

        suspend fun clearTrash() {
            appDatabase.notesDao().deleteTrash()
        }

        suspend fun restoreAllNotesFromTrash() {
            appDatabase.notesDao().restoreAllItems()
        }

//    }

        suspend fun insert(note: Note) {
            appDatabase.notesDao().insert(note)
            appDatabase.notesDao().insertPosition()
        }

        suspend fun update(note: Note) {
            appDatabase.notesDao().update(note)
        }

        suspend fun delete(note: Note) {
            appDatabase.notesDao().delete(note)
        }
    }