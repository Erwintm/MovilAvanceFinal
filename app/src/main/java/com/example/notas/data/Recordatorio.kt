package com.example.notas.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recordatorios",
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["notaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("notaId")]
)
data class Recordatorio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val titulo: String,
    val descripcion: String,
    val fechaRecordatorio: Long,   // fecha u hora como timestamp
    val notaId: Int,               // ← NO nullable
    val createdAt: Long = System.currentTimeMillis()
)
