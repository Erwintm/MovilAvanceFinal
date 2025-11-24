package com.example.notas.data

import kotlinx.coroutines.flow.Flow

class OfflineNoteRepository(
  private val noteDao: NoteDao,

  private val multimediaDao: MultimediaDao
) : NoteRepository {


  override fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
  override suspend fun insert(note: Note) = noteDao.insert(note)
  override suspend fun update(note: Note) = noteDao.updateNote(note)
  override suspend fun delete(note: Note) = noteDao.deleteNote(note)


  override fun getMultimediaForNota(notaId: Int): Flow<List<Multimedia>> =
    multimediaDao.getMultimediaForNota(notaId)

  override suspend fun insertMultimedia(multimedia: Multimedia) =
    multimediaDao.insert(multimedia)

  override suspend fun deleteMultimedia(multimedia: Multimedia) =
    multimediaDao.delete(multimedia)
}