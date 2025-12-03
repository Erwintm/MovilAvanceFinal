package com.example.notas.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notas.data.Note
import com.example.notas.data.NoteRepository
import com.example.notas.data.Multimedia
import kotlinx.coroutines.launch


/**
 * ViewModel para la pantalla de añadir/editar notas.
 * Se encarga de contener el estado de los campos de entrada y de guardar la nota en la base de datos.
 * @param repository El repositorio de datos para interactuar con la base de datos (Room).
 */
class AddNoteViewModel(private val repository: NoteRepository) : ViewModel() {
//---Variables de estado (State Variables)

    var title by mutableStateOf("")
        private set

    var description by mutableStateOf("")
        private set

    var seleccionarTipo by mutableStateOf("Notes")
        private set

    var fechaLimite by mutableStateOf("")
        private set

    var hora by mutableStateOf("")
        private set

    private val estado by mutableStateOf("Pendiente")
//---------------------------------------------

    val isEntryValid: Boolean
        get() = title.isNotBlank() && description.isNotBlank()

    // --- Métodos de Actualización (Llamados por la UI) ---

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


    /**
     * Crea un objeto Note, lo guarda en la base de datos y luego inserta las entradas
     * de Multimedia (imágenes, audios, etc.) asociadas a esa nota.
     * * @param tempImageUris Lista de URIs de imágenes adjuntas (recibida desde la UI).
     */
    fun saveNoteWithMultimedia(tempImageUris: List<String>) {
        if (isEntryValid) {

            val tipo = if (seleccionarTipo == "Notes") 1 else 2

            // 1. Construye el objeto Note.
            val noteToSave = Note(
                id = 0,
                title = title.ifBlank { "(sin título)" },
                description = description,
                idTipo = tipo,

                fechaLimite = if (tipo == 2) fechaLimite.ifBlank { null } else null,
                hora = if (tipo == 2) hora.ifBlank { null } else null,
                estado = estado
            )



            viewModelScope.launch {


                val newNoteId = repository.insert(noteToSave)



                tempImageUris.forEach { uri ->
                    val multimediaEntry = Multimedia(
                        id = 0,

                        notaId = newNoteId.toInt(),
                        uriArchivo = uri,
                        tipo = "IMAGEN"
                    )

                    repository.insertMultimedia(multimediaEntry)
                }
            }
        }
    }
}