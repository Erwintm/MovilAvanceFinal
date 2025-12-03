package com.example.notas.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatorioDao {
    @Query("SELECT * FROM recordatorios WHERE notaId = :id")
    fun getAll(id: Int): Flow<List<Recordatorio>>

    @Query("SELECT * FROM recordatorios WHERE notaId = :id")
    fun getByNota(id: Int): Flow<List<Recordatorio>>

    @Insert
    suspend fun insert(recordatorio: Recordatorio)

    @Delete
    suspend fun delete(recordatorio: Recordatorio)
    @Update
    suspend fun update(recordatorio: Recordatorio)
    @Query("SELECT * FROM recordatorios WHERE id = :id LIMIT 1")
    fun getById(id: Int): Flow<Recordatorio?>
    @Query("SELECT * FROM recordatorios")
    suspend fun getAllDirect(): List<Recordatorio>

}