package com.example.notas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MultimediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(multimedia: Multimedia)

    @Delete
    suspend fun delete(multimedia: Multimedia)


    @Query("SELECT * FROM multimedia WHERE notaId = :notaId ORDER BY fechaCreacion ASC")
    fun getMultimediaForNota(notaId: Int): Flow<List<Multimedia>>
}