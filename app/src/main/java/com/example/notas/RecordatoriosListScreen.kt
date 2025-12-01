package com.example.notas

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.notas.viewmodel.RecordatorioViewModel

@Composable
fun RecordatoriosListScreen(
    navController: NavController,
    noteId: Int,
    recordatorioViewModel: RecordatorioViewModel
) {
    Button(onClick = { navController.popBackStack("main", inclusive = false) }) {
        Text(stringResource(R.string.regresar))
    }
    val lista by recordatorioViewModel
        .getRecordatoriosByNota(noteId)
        .collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("addRecordatorio/$noteId")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo recordatorio")
            }
        }
    ) { padding ->

        LazyColumn(modifier = Modifier.padding(padding)) {
            items(lista) { recordatorio ->
                RecordatorioItem(
                    recordatorio,
                    onDelete = { recordatorioViewModel.delete(recordatorio) },
                    onEdit = {
                        navController.navigate("editRecordatorio/${recordatorio.id}")
                    }
                )
            }
        }
    }
}
