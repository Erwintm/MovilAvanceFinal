package com.example.notas



import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notas.data.NoteRepository
import com.example.notas.viewmodel.NoteViewModel

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
