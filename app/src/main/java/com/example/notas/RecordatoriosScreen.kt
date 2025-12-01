package com.example.notas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.notas.alarmas.AlarmItem
import com.example.notas.alarmas.AlarmScheduler
import com.example.notas.viewmodel.RecordatorioViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordatoriosScreen(
    noteId: Int,
    navController: NavController,
    viewModel: RecordatorioViewModel,
    alarmScheduler: AlarmScheduler
) {
    val recordatorios by viewModel.getRecordatoriosByNota(noteId)
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recordatorios") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("addRecordatorio/$noteId")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(12.dp)
        ) {
            items(recordatorios) { rec ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(Modifier.padding(15.dp)) {

                        Text(text = rec.titulo, style = MaterialTheme.typography.titleMedium)
                        Text(text = rec.descripcion, style = MaterialTheme.typography.bodyMedium)

                        val fecha = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(rec.fechaRecordatorio),
                            ZoneId.systemDefault()
                        )
                        Text(
                            text = stringResource(R.string.fecha)+ "$fecha",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {

                            // EDITAR
                            TextButton(onClick = {
                                navController.navigate("editRecordatorio/${rec.id}")
                            }) {
                                Text(stringResource(R.string.editar))
                            }

                            // ELIMINAR
                            TextButton(onClick = {
                                viewModel.delete(rec)

                                // cancelar alarma
                                alarmScheduler.cancel(
                                    AlarmItem(
                                        noteId = rec.notaId,
                                        alarmTime = fecha,
                                        title = rec.titulo,
                                        description = rec.descripcion
                                    )
                                )
                            }) {
                                Text(stringResource(R.string.eliminar))
                            }
                        }
                    }
                }
            }
        }
    }
}
