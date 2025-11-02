package com.example.notas.data

import kotlinx.coroutines.flow.Flow
import kotlin.text.insert

class OfflineNoteRepository(private val noteDao: NoteDao): NoteRepository {
  override  fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    override suspend fun insert(note: Note) = noteDao.insert(note)
    override  suspend fun update(note: Note) = noteDao.updateNote(note)
    override  suspend fun delete(note: Note) = noteDao.deleteNote(note)

}