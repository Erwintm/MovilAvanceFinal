package com.example.notas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notas.data.Multimedia // 👈 Importar la nueva entidad
import com.example.notas.data.Note
import com.example.notas.data.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File // 👈 Necesario para eliminar el archivo físico

class NoteDetailViewModel(private val repository: NoteRepository) : ViewModel() {

    // ----------------------------------------------------
    // NUEVAS PROPIEDADES PARA MULTIMEDIA
    // ----------------------------------------------------

    // Almacena el ID de la Nota/Tarea actual (se inicializa al entrar a la pantalla)
    private var currentNoteId: Int = 0

    // Lista observable de todos los archivos multimedia asociados a la nota actual
    lateinit var multimediaList: StateFlow<List<Multimedia>>

    fun initialize(noteId: Int) {
        if (noteId != currentNoteId) {
            currentNoteId = noteId

            // Inicializar la StateFlow que obtendrá la lista del Repositorio
            multimediaList = repository.getMultimediaForNota(noteId)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList() // Inicialmente vacía
                )
        }
    }

    // ----------------------------------------------------
    // MÉTODOS DE ACCIÓN
    // ----------------------------------------------------

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            // OPTIMIZACIÓN: Idealmente, deberías eliminar primero los archivos físicos
            // asociados a esta nota antes de eliminar la nota de la BD.
            repository.delete(note)
        }
    }

    fun updateStatus(note: Note, newStatus: String) {
        // ... (Tu lógica existente para actualizar el estado)
    }

    /**
     * Elimina un archivo multimedia de la BD y, crucialmente, del disco.
     * @param multimedia El objeto Multimedia a eliminar.
     * @param storagePath La ruta base de almacenamiento para construir la ruta absoluta.
     */
    fun deleteMultimedia(multimedia: Multimedia, contextFilesDir: File) {
        viewModelScope.launch {
            // 1. Eliminar el archivo físico del disco (Requisito: Archivos)
            try {
                // Asume que uriArchivo es una ruta relativa o un nombre de archivo.
                // Si es una ruta absoluta, se usaría directamente new File(multimedia.uriArchivo).
                val fileToDelete = File(contextFilesDir, multimedia.uriArchivo)
                if (fileToDelete.exists()) {
                    fileToDelete.delete()
                }
            } catch (e: Exception) {
                // Manejar el error de eliminación de archivo
                e.printStackTrace()
            }

            // 2. Eliminar el registro de la base de datos (CRUD n Multimedia)
            repository.deleteMultimedia(multimedia)
        }
    }

    /**
     * Inserta un nuevo registro multimedia después de que se ha capturado un archivo.
     */
    fun insertMultimedia(multimedia: Multimedia) {
        viewModelScope.launch {
            repository.insertMultimedia(multimedia)
        }
    }
}