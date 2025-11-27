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



    private val _currentNoteId = MutableStateFlow(0)

    //  Estado reactivo para la NOTA completa. Se actualiza cada vez que el ID cambia.
    val note: StateFlow<Note> = _currentNoteId
        .flatMapLatest { noteId ->
            if (noteId == 0) {
                // Devuelve una nota vacía y segura si el ID es 0.
                flowOf(Note(id = 0, title = "", description = "", idTipo = 1))
            } else {
                // Obtenemos el flujo de la nota real del repositorio.
                repository.getNoteById(noteId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Note(id = 0, title = "", description = "", idTipo = 1)
        )

    //  Estado reactivo para la lista de MULTIMEDIA
    val multimediaList: StateFlow<List<Multimedia>> = _currentNoteId
        .flatMapLatest { noteId ->
            if (noteId == 0) {
                flowOf(emptyList())
            } else {
                repository.getMultimediaForNota(noteId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )



    // La función initialize solo necesita actualizar el ID.
    fun initialize(noteId: Int) {
        _currentNoteId.value = noteId
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {

            repository.delete(note)
        }
    }


    fun deleteMultimedia(multimedia: Multimedia, contextFilesDir: File) {
        viewModelScope.launch {
            //  Eliminar el archivo físico del disco (Requisito: Archivos)
            try {
                // Paa Audio/Video: están en filesDir (debes usar contextFilesDir)
                // Para Imagen de Galería: La URI es externa, no se elimina del disco.
                // Asumo que solo Audio y Video se guardan en filesDir
                val fileToDelete = File(contextFilesDir, multimedia.uriArchivo)
                if (fileToDelete.exists()) {
                    fileToDelete.delete()
                }
            } catch (e: Exception) {
                // Manejar error de eliminación de archivo
                e.printStackTrace()
            }

            // iminar el registro de la base de datos (CRUD n Multimedia)
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