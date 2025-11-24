package com.example.notas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notas.data.Note
import com.example.notas.data.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class MainViewModel(private val repository: NoteRepository) : ViewModel() {

//
    private val _allNotesFlow = repository.getAllNotes()

    //Almacena el texto ingresado en el OutlinedTextField de la MainScreen.
    private val _searchText = MutableStateFlow("")

    //Permite que la UI escuche los cambios en el texto de búsqueda (.collectAsState()).
    val searchText: StateFlow<String> = _searchText

//Almacena el ID del filtro seleccionado (0=Todas, 1=Notas, 2=Tareas) desde los RadioButton.
    private val _filterType = MutableStateFlow(0)

    //Permite que la UI escuche qué filtro está activo.
    val filterType: StateFlow<Int> = _filterType

    //Logica


    val filteredNotes: StateFlow<List<Note>> = combine(
        _allNotesFlow,
        _searchText,
        _filterType
    ) { notes, query, filterId ->

// A. Filtrado por Tipo (Radio Button)

        val filteredByType = when (filterId) {
            1 -> notes.filter { it.idTipo == 1 }
            2 -> notes.filter { it.idTipo == 2 }
            else -> notes
        }

// B. Filtrado por Búsqueda (TextField)
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) {
            filteredByType
        } else {
            filteredByType.filter {
                it.title.contains(trimmedQuery, ignoreCase = true) ||
                        it.description.contains(trimmedQuery, ignoreCase = true)
            }
        }
    }

        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
//Estos son los métodos que la Vista (UI) llama para actualizar el estado del ViewModel (los que vimos en MainScreen).

    //Llamado por el OutlinedTextField. Simplemente actualiza el valor de _searchText,
    // lo que a su vez dispara la función combine para recalcular filteredNotes.
    fun updateSearchText(text: String) {
        _searchText.value = text
    }
//Llamado por los RadioButton. Actualiza _filterType, lo que también dispara la función combine.
    fun updateFilterType(type: Int) {
        _filterType.value = type
    }

    //Llamado desde la pantalla de detalle (NoteDetailScreen).
    // Inicia una corrutina (viewModelScope.launch) para llamar al Repositorio y eliminar la nota de la base de datos.
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }
}