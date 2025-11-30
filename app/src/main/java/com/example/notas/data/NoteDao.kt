package com.example.notas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    /**
     * CORRECCIÓN CLAVE: Ahora devuelve el ID (Long) de la nota recién insertada.
     * El ViewModel usa este ID para enlazar los archivos Multimedia.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Int): Flow<Note>

    @Query("SELECT * FROM notes WHERE idTipo = 2 ORDER BY fechaLimite ASC")
    fun getAllTasks(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE idTipo = 1 ORDER BY id DESC")
    fun getAllNotesOnly(): Flow<List<Note>>
}