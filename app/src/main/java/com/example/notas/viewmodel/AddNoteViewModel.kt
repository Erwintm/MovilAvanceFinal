package com.example.notas.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notas.data.Note
import com.example.notas.data.NoteRepository
import kotlinx.coroutines.launch

// El constructor recibe el repositorio para poder guardar los datos
class AddNoteViewModel(private val repository: NoteRepository) : ViewModel() {

    // 1. VARIABLES DE ESTADO (UI State)
    // Usamos 'by' para que la lectura/escritura sea directa (delegación de propiedades)

    var title by mutableStateOf("")
        private set // Solo se modifica internamente en este ViewModel

    var description by mutableStateOf("")
        private set

    var imageUri by mutableStateOf<String?>(null)
        private set

    // Controla si es "Notes" (1) o "Tasks" (2)
    var seleccionarTipo by mutableStateOf("Notes")
        private set

    var fechaLimite by mutableStateOf("")
        private set

    var hora by mutableStateOf("")
        private set

    // El estado de una nueva nota/tarea es siempre "Pendiente" por defecto
    private val estado by mutableStateOf("Pendiente")

    // 2. FUNCIÓN DE VALIDACIÓN (Logic)
    // Propiedad calculada que la UI usará para habilitar el botón de guardar.
    val isEntryValid: Boolean
        get() = title.isNotBlank() && description.isNotBlank()


    // 3. EVENT HANDLERS (Funciones llamadas por el Composable)

    fun updateTitle(newTitle: String) {
        title = newTitle
    }

    fun updateDescription(newDescription: String) {
        description = newDescription
    }

    fun updateTipo(newTipo: String) {
        seleccionarTipo = newTipo
        // Limpiamos campos de tarea si cambia a nota, para evitar errores
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

    fun setImageUri(uri: String?) {
        imageUri = uri
    }

    // 4. LÓGICA DE NEGOCIO (Guardado)
    fun saveNote() {
        if (isEntryValid) {
            val tipo = if (seleccionarTipo == "Notes") 1 else 2

            // Creamos el objeto Note usando las variables de estado del ViewModel
            val noteToSave = Note(
                // id se deja en 0, Room lo asignará automáticamente
                id = 0,
                title = title.ifBlank { "(sin título)" },
                description = description,
                imageUri = imageUri,
                idTipo = tipo,

                // Los campos de tarea solo se guardan si idTipo es 2
                fechaLimite = if (tipo == 2) fechaLimite.ifBlank { null } else null,
                hora = if (tipo == 2) hora.ifBlank { null } else null,
                estado = estado
            )

            // Llamada al repositorio en un hilo seguro
            viewModelScope.launch {
                repository.insert(noteToSave)
            }

            // Opcional: Limpiar el estado después de guardar si quieres que el ViewModel
            // se pueda reutilizar inmediatamente (aunque lo normal es que se destruya).
        }
    }
}