// Archivo: NoteViewModelFactory.kt (ACTUALIZADO)
package com.example.notas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notas.data.NoteRepository
import com.example.notas.viewmodel.NoteViewModel
import com.example.notas.viewmodel.AddNoteViewModel
import com.example.notas.viewmodel.EditNoteViewModel // 👈 NUEVA IMPORTACIÓN

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Lógica para ViewModels de Acceso a Datos
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }

        // Lógica para ViewModels de la UI (Add/Edit)
        if (modelClass.isAssignableFrom(AddNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddNoteViewModel(repository) as T
        }

        // 💡 NUEVA LÓGICA: Ahora sabe cómo crear EditNoteViewModel
        if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditNoteViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}