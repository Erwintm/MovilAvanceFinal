package com.example.notas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notas.data.Note
import com.example.notas.data.NoteRepository
import kotlinx.coroutines.launch

class NoteDetailViewModel(private val repository: NoteRepository) : ViewModel() {


    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }


    fun updateStatus(note: Note, newStatus: String) {

        if (note.idTipo == 2) {
            val updatedNote = note.copy(estado = newStatus)
            viewModelScope.launch {
                repository.update(updatedNote)
            }
        }
    }
}