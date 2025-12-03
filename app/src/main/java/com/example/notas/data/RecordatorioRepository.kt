package com.example.notas.data

import kotlinx.coroutines.flow.Flow

class RecordatorioRepository(private val recordatorioDao: RecordatorioDao) {
    fun getRecordatorioById(id: Int): Flow<Recordatorio?> =
        recordatorioDao.getById(id)

    fun getAllRecordatorios(id: Int): Flow<List<Recordatorio>> =
        recordatorioDao.getAll(id)

    fun getRecordatoriosByNota(notaId: Int): Flow<List<Recordatorio>> =
        recordatorioDao.getByNota(notaId)

    suspend fun insert(recordatorio: Recordatorio) =
        recordatorioDao.insert(recordatorio)

    suspend fun delete(recordatorio: Recordatorio) =
        recordatorioDao.delete(recordatorio)
    suspend fun update(recordatorio: Recordatorio) =
        recordatorioDao.update(recordatorio)
    suspend fun getAllRecordatoriosDirect(): List<Recordatorio> =
        recordatorioDao.getAllDirect()
}