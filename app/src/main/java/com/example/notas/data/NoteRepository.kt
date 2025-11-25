package com.example.notas.data

import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getAllNotes(): Flow<List<Note>>
    suspend fun insert(note: Note)
    suspend fun update(note: Note)
    suspend fun delete(note: Note)

    fun getMultimediaForNota(notaId: Int): Flow<List<Multimedia>>
    suspend fun insertMultimedia(multimedia: Multimedia)
    suspend fun deleteMultimedia(multimedia: Multimedia)
}
