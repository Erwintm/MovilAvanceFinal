package com.example.notas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notas.data.NoteRepository
import com.example.notas.viewmodel.AddNoteViewModel
import com.example.notas.viewmodel.EditNoteViewModel
import com.example.notas.viewmodel.MainViewModel
import com.example.notas.viewmodel.NoteDetailViewModel

import com.example.notas.viewmodel.NoteViewModel

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {


        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }


        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }

        if (modelClass.isAssignableFrom(AddNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddNoteViewModel(repository) as T
        }

        // 4. ViewModel de Editar Nota (EditNote)
        if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditNoteViewModel(repository) as T
        }


        if (modelClass.isAssignableFrom(NoteDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteDetailViewModel(repository) as T
        }


        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}