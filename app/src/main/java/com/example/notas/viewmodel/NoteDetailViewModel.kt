package com.example.notas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notas.data.Multimedia
import com.example.notas.data.Note
import com.example.notas.data.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class NoteDetailViewModel(private val repository: NoteRepository) : ViewModel() {

    // ----------------------------------------------------
    // CORRECCIÓN CLAVE: Inicialización Segura de Multimedia
    // ----------------------------------------------------

    // 1. Usamos un flujo mutable para rastrear el ID de la nota (inicialmente 0)
    private val _currentNoteId = MutableStateFlow(0)

    // 2. Inicialización segura de multimediaList (SIEMPRE tiene un valor)
    val multimediaList: StateFlow<List<Multimedia>> = _currentNoteId
        .flatMapLatest { noteId ->
            if (noteId == 0) {
                // Si el ID es 0, devuelve un flujo vacío para evitar errores.
                flowOf(emptyList())
            } else {
                // Si el ID es válido, obtenemos el flujo real del repositorio.
                repository.getMultimediaForNota(noteId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // Siempre comienza con una lista vacía segura.
        )

    // ----------------------------------------------------
    // MÉTODOS DE INICIALIZACIÓN Y ACCIÓN
    // ----------------------------------------------------

    // La función initialize solo necesita actualizar el ID.
    fun initialize(noteId: Int) {
        _currentNoteId.value = noteId
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            // NOTA: Implementar lógica para eliminar archivos multimedia asociados
            // antes de eliminar la nota si es necesario.
            repository.delete(note)
        }
    }

    // NOTA: Si tenías 'updateStatus', asegúrate de agregarlo aquí si lo necesitas.
    // fun updateStatus(note: Note, newStatus: String) { ... }

    /**
     * Elimina un archivo multimedia de la BD y, crucialmente, del disco.
     */
    fun deleteMultimedia(multimedia: Multimedia, contextFilesDir: File) {
        viewModelScope.launch {
            // 1. Eliminar el archivo físico del disco (Requisito: Archivos)
            try {
                val fileToDelete = File(contextFilesDir, multimedia.uriArchivo)
                if (fileToDelete.exists()) {
                    fileToDelete.delete()
                }
            } catch (e: Exception) {
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