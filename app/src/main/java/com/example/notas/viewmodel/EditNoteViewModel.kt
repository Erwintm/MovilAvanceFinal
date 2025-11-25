package com.example.notas.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notas.data.Note
import com.example.notas.data.NoteRepository
import kotlinx.coroutines.launch


class EditNoteViewModel(private val repository: NoteRepository) : ViewModel() {


    var noteId by mutableStateOf(0)
        private set
    var title by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set
    var imageUri by mutableStateOf<String?>(null)
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
//Se llama una vez desde la EditNoteScreen (LaunchedEffect) para precargar el formulario
    fun initializeState(note: Note) {
        noteId = note.id
        title = note.title
        description = note.description
        imageUri = note.imageUri
        // Determina si es "Notes" (1) o "Tasks" (2)
        seleccionarTipo = if (note.idTipo == 1) "Notes" else "Tasks"
        fechaLimite = note.fechaLimite ?: ""
        hora = note.hora ?: ""
        estado = note.estado ?: "Pendiente"
    }

    //Simplemente actualizan el estado interno correspondiente (ej: al escribir en un campo de texto).
    fun updateTitle(newTitle: String) { title = newTitle }
    fun updateDescription(newDescription: String) { description = newDescription }
    fun updateTipo(newTipo: String) { seleccionarTipo = newTipo }
    fun updateFechaLimite(newDate: String) { fechaLimite = newDate }
    fun updateHora(newTime: String) { hora = newTime }
    fun updateEstado(newStatus: String) { estado = newStatus }
    fun updateImageUri(uri: String?) { imageUri = uri }

    fun buildUpdatedNote(): Note {
        val tipo = if (seleccionarTipo == "Notes") 1 else 2


        return Note(
            id = noteId,
            title = title.ifBlank { "(sin título)" },
            description = description,
            imageUri = imageUri,
            idTipo = tipo,


            fechaLimite = if (tipo == 2) fechaLimite.ifBlank { null } else null,
            hora = if (tipo == 2) hora.ifBlank { null } else null,
            estado = if (tipo == 2) estado.ifBlank { "Pendiente" } else null
        )
    }

    fun updateNote() {
        if (isEntryValid) {
            val noteToUpdate = buildUpdatedNote()

            viewModelScope.launch {
                repository.update(noteToUpdate)
            }
        }
    }
}