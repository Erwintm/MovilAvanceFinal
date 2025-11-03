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


    private val _allNotesFlow = repository.getAllNotes()


    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText


    private val _filterType = MutableStateFlow(0)
    val filterType: StateFlow<Int> = _filterType


    val filteredNotes: StateFlow<List<Note>> = combine(
        _allNotesFlow,
        _searchText,
        _filterType
    ) { notes, query, filterId ->



        val filteredByType = when (filterId) {
            1 -> notes.filter { it.idTipo == 1 }
            2 -> notes.filter { it.idTipo == 2 }
            else -> notes
        }


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

    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    fun updateFilterType(type: Int) {
        _filterType.value = type
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }
}