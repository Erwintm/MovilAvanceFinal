package com.example.notas.data

import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getAllNotes(): Flow<List<Note>>

    /**
     * CORRECCIÓN CLAVE: La interfaz ahora espera que insert devuelva el ID (Long).
     */
    suspend fun insert(note: Note): Long

    suspend fun update(note: Note)
    suspend fun delete(note: Note)


    fun getNoteById(id: Int): Flow<Note>
    fun getMultimediaForNota(notaId: Int): Flow<List<Multimedia>>
    suspend fun insertMultimedia(multimedia: Multimedia)
    suspend fun deleteMultimedia(multimedia: Multimedia)
}