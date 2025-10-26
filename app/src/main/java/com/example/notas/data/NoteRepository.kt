package com.example.notas.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    fun getAllTasks(): Flow<List<Note>> = noteDao.getAllTasks()
    fun getAllNotesOnly(): Flow<List<Note>> = noteDao.getAllNotesOnly()

    suspend fun insert(note: Note) = noteDao.insert(note)
    suspend fun update(note: Note) = noteDao.updateNote(note)
    suspend fun delete(note: Note) = noteDao.deleteNote(note)
}
