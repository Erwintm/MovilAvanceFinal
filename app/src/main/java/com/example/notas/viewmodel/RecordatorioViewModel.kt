package com.example.notas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notas.data.Recordatorio
import com.example.notas.data.RecordatorioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RecordatorioViewModel( private val repository: RecordatorioRepository): ViewModel()  {
    fun getRecordatoriosByNota(notaId: Int): Flow<List<Recordatorio>> =
        repository.getRecordatoriosByNota(notaId)

    fun insert(recordatorio: Recordatorio) = viewModelScope.launch {
        repository.insert(recordatorio)
    }

    fun delete(recordatorio: Recordatorio) = viewModelScope.launch {
        repository.delete(recordatorio)
    }
    fun update(recordatorio: Recordatorio) = viewModelScope.launch {
        repository.update(recordatorio)
    }
    fun getRecordatorioById(id: Int): Flow<Recordatorio?> {
        return repository.getRecordatorioById(id)
    }

}