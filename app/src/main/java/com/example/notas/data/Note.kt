package com.example.notas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val imageUri: String? = null,
    val idTipo: Int = 1, // 1 = Nota, 2 = Tarea
    val fechaLimite: String? = null,
    val hora: String? = null,
    val estado: String? = "Pendiente"
)