package com.example.notas.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    suspend fun insert(note: Note) = noteDao.insert(note)

    suspend fun delete(note: Note) = noteDao.deleteNote(note)
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }
}
