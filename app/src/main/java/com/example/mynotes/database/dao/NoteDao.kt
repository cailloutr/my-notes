package com.example.mynotes.database.dao

import androidx.room.*
import com.example.mynotes.database.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM note WHERE is_trash != 1 ORDER BY position")
    fun getAllSavedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE is_trash = 1 ORDER BY position")
    fun getAllTrashNotes(): Flow<List<Note>>

    @Query("DELETE FROM note WHERE is_trash = 1")
    suspend fun deleteTrash()

    @Query("UPDATE Note SET is_trash = 0 WHERE is_trash = 1")
    suspend fun restoreAllItems()

/*    @Query("UPDATE Note SET position = CASE position WHEN :initPosition THEN :finalPosition WHEN :finalPosition THEN :initPosition ELSE position END")
    suspend fun swipePositions(initPosition: Long, finalPosition: Long)

    @Query("UPDATE Note SET position = :newPosition WHERE position = :oldPosition")
    suspend fun updatePosition(oldPosition: Int, newPosition: Int)

    @Query("UPDATE Note SET position = (position+1) WHERE position > :target")
    suspend fun updatePositionUp1(target: Long)

    @Query("UPDATE Note SET position = :target+1 WHERE position = :item")
    suspend fun updatePositionUp2(item: Long, target: Long)

    @Query("UPDATE Note SET position = (position - 1) WHERE position > :item")
    suspend fun updatePositionUp3(item: Long)

    //TODO: Move down to the last position is not working
    @Query("UPDATE Note SET position = (position+1) WHERE position >= :target")
    suspend fun updatePositionDown1(target: Long)

    @Query("UPDATE Note SET position = :target WHERE position = :item + 1")
    suspend fun updatePositionDown2(item: Long, target: Long)

    @Query("UPDATE Note SET position = (position - 1) WHERE position > :item")
    suspend fun updatePositionDown3(item: Long)


*/

    @Query("UPDATE Note SET position = (id)")
    suspend fun insertPosition()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(vararg note: Note)

    @Delete
    suspend fun delete(vararg note: Note)

}