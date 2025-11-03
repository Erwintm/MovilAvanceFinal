package com.example.notas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notas.data.Note
import com.example.notas.data.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine // 👈 ¡Importación clave!
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Este ViewModel ahora utiliza 'combine' para que el filtro y la búsqueda funcionen reactivamente.
class MainViewModel(private val repository: NoteRepository) : ViewModel() {

    // 1. ESTADO DEL REPOSITORIO: Lista de todas las notas (Flow de Room)
    private val _allNotesFlow = repository.getAllNotes()

    // 2. ESTADO DE LA UI: Variables para filtrar o buscar
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    // Filtro por tipo: 1=Notas, 2=Tareas, 0=Todos
    private val _filterType = MutableStateFlow(0)
    val filterType: StateFlow<Int> = _filterType

    // 3. ESTADO COMBINADO: La lista que la UI consumirá
    // Se combina el flujo de la base de datos con los dos flujos de estado de la UI.
    val filteredNotes: StateFlow<List<Note>> = combine(
        _allNotesFlow,
        _searchText,
        _filterType
    ) { notes, query, filterId ->
        // Esta lambda se ejecuta cada vez que cambia la lista, la búsqueda o el filtro.

        // 3.1. Aplicar filtro por tipo
        val filteredByType = when (filterId) {
            1 -> notes.filter { it.idTipo == 1 } // Solo Notas
            2 -> notes.filter { it.idTipo == 2 } // Solo Tareas
            else -> notes // Todos
        }

        // 3.2. Aplicar filtro de búsqueda (si hay texto)
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
        // Convierte el Flow en un StateFlow que se mantiene vivo.
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 4. HANDLERS DE EVENTOS (Llamados por la UI)
    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    fun updateFilterType(type: Int) {
        _filterType.value = type
    }

    // 5. FUNCIÓN DE ELIMINACIÓN
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }
}