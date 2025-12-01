package com.example.notas.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notas.data.Multimedia
import com.example.notas.data.Note
import com.example.notas.data.NoteRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


class EditNoteViewModel(private val repository: NoteRepository) : ViewModel() {


    var noteId by mutableStateOf(0)
        private set
    var title by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set

    // ELIMINADO: imageUri, ahora usamos la tabla Multimedia

    // Lista para almacenar los URIs de multimedia existentes para visualización.
    var multimediaUris by mutableStateOf<List<String>>(emptyList())
        private set

    var seleccionarTipo by mutableStateOf("Notes")
        private set
    var fechaLimite by mutableStateOf("")
        private set
    var hora by mutableStateOf("")
        private set
    var estado by mutableStateOf("Pendiente")
        private set


    val isEntryValid: Boolean
        get() = title.isNotBlank() && description.isNotBlank()


    // Función de inicialización modificada para cargar también los archivos multimedia
    fun initializeState(note: Note) {
        noteId = note.id
        title = note.title
        description = note.description
        // Determina si es "Notes" (1) o "Tasks" (2)
        seleccionarTipo = if (note.idTipo == 1) "Notes" else "Tasks"
        fechaLimite = note.fechaLimite ?: ""
        hora = note.hora ?: ""
        estado = note.estado ?: "Pendiente"

        // Cargar Multimedia asociada a esta nota
        loadMultimedia()
    }

    /**
     * Carga los URIs de multimedia asociados a la nota actual para mostrarlos en la UI.
     */
    private fun loadMultimedia() {
        if (noteId != 0) {
            viewModelScope.launch {
                // Obtiene la lista de multimedia de la nota y actualiza el estado
                repository.getMultimediaForNota(noteId).collect { multimediaList ->
                    multimediaUris = multimediaList.map { it.uriArchivo }
                }
            }
        }
    }

    // Simplemente actualizan el estado interno correspondiente (ej: al escribir en un campo de texto).
    fun updateTitle(newTitle: String) { title = newTitle }
    fun updateDescription(newDescription: String) { description = newDescription }
    fun updateTipo(newTipo: String) {
        seleccionarTipo = newTipo
        // Lógica para limpiar campos si cambia a Nota (idTipo=1)
        if (newTipo == "Notes") {
            fechaLimite = ""
            hora = ""
            estado = "Pendiente"
        }
    }
    fun updateFechaLimite(newDate: String) { fechaLimite = newDate }
    fun updateHora(newTime: String) { hora = newTime }
    fun updateEstado(newStatus: String) { estado = newStatus }

    // ELIMINADA: updateImageUri


    fun buildUpdatedNote(): Note {
        val tipo = if (seleccionarTipo == "Notes") 1 else 2


        return Note(
            id = noteId,
            title = title.ifBlank { "(sin título)" },
            description = description,
            // imageUri ELIMINADO
            idTipo = tipo,


            fechaLimite = if (tipo == 2) fechaLimite.ifBlank { null } else null,
            hora = if (tipo == 2) hora.ifBlank { null } else null,
            estado = if (tipo == 2) estado.ifBlank { "Pendiente" } else null
        )
    }

    /**
     * Actualiza la nota principal y maneja la adición de nuevos archivos multimedia.
     * La lógica de borrado de archivos multimedia existentes requiere más complejidad y se omite aquí por ahora.
     * * @param newImageUris Lista de URIs de las nuevas imágenes que el usuario adjuntó en esta sesión de edición.
     */
    fun updateNoteWithMultimedia(newImageUris: List<String>) {
        if (isEntryValid) {
            val noteToUpdate = buildUpdatedNote()

            viewModelScope.launch {
                // 1. Actualizar la nota principal
                repository.update(noteToUpdate)

                // 2. Insertar solo los nuevos archivos multimedia (sin borrar los existentes).
                newImageUris.forEach { uri ->
                    val multimediaEntry = Multimedia(
                        id = 0,
                        notaId = noteId, // Usa el ID de la nota existente
                        uriArchivo = uri,
                        tipo = "IMAGEN"
                    )
                    repository.insertMultimedia(multimediaEntry)
                }
            }
        }
    }
}