package com.example.notas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notas.data.RecordatorioRepository
import com.example.notas.viewmodel.RecordatorioViewModel




class RecordatorioViewModelFactory (private val repository: RecordatorioRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordatorioViewModel::class.java)) {
            return RecordatorioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}