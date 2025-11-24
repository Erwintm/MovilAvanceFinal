package com.example.notas.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "multimedia",
    foreignKeys = [
        ForeignKey(
            entity = Note::class, // Enlaza a tu entidad principal Note/Tarea
            parentColumns = ["id"],
            childColumns = ["notaId"],
            onDelete = ForeignKey.CASCADE // Si se elimina la Nota, se eliminan sus archivos multimedia
        )
    ]
)
data class Multimedia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val notaId: Int,

    val uriArchivo: String,

    val tipo: String,

    val fechaCreacion: Long = System.currentTimeMillis()
)