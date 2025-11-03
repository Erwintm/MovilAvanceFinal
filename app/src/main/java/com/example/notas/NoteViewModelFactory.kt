package com.example.notas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notas.data.NoteRepository
import com.example.notas.viewmodel.AddNoteViewModel
import com.example.notas.viewmodel.EditNoteViewModel
import com.example.notas.viewmodel.MainViewModel

import com.example.notas.viewmodel.NoteViewModel

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        // 1. ViewModel de Datos General (Si aún se usa)
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }

        // 2. ViewModel de Pantalla Principal (MainScreen)
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }

        // 3. ViewModel de Agregar Nota (AddNote)
        if (modelClass.isAssignableFrom(AddNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddNoteViewModel(repository) as T
        }

        // 4. ViewModel de Editar Nota (EditNote)
        if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditNoteViewModel(repository) as T
        }

        // 5. ViewModel de Detalle de Nota (NoteDetail)


        // Si no es ninguna de las clases conocidas, lanza una excepción
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}