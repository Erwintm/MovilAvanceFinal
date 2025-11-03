package com.example.notas.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notas.data.Note
import com.example.notas.data.NoteRepository
import kotlinx.coroutines.launch


class AddNoteViewModel(private val repository: NoteRepository) : ViewModel() {

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
    private val estado by mutableStateOf("Pendiente")

    val isEntryValid: Boolean
        get() = title.isNotBlank() && description.isNotBlank()

    fun updateTitle(newTitle: String) {
        title = newTitle
    }

    fun updateDescription(newDescription: String) {
        description = newDescription
    }

    fun updateTipo(newTipo: String) {
        seleccionarTipo = newTipo

        if (newTipo == "Notes") {
            fechaLimite = ""
            hora = ""
        }
    }

    fun updateFechaLimite(newDate: String) {
        fechaLimite = newDate
    }

    fun updateHora(newTime: String) {
        hora = newTime
    }

    fun updateImageUri(uri: String?) {
        imageUri = uri
    }

    fun saveNote() {
        if (isEntryValid) {
            val tipo = if (seleccionarTipo == "Notes") 1 else 2

            val noteToSave = Note(
                id = 0,
                title = title.ifBlank { "(sin título)" },
                description = description,
                imageUri = imageUri,
                idTipo = tipo,


                fechaLimite = if (tipo == 2) fechaLimite.ifBlank { null } else null,
                hora = if (tipo == 2) hora.ifBlank { null } else null,
                estado = estado
            )


            viewModelScope.launch {
                repository.insert(noteToSave)
            }

        }
    }
}