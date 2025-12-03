package com.example.notas

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.notas.viewmodel.RecordatorioViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordatoriosListScreen(
    navController: NavController,
    noteId: Int,
    recordatorioViewModel: RecordatorioViewModel
) {

    val lista by recordatorioViewModel
        .getRecordatoriosByNota(noteId)
        .collectAsState(initial = emptyList())

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD205121A)),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("addRecordatorio/$noteId")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo recordatorio")
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { navController.popBackStack("main", inclusive = false) }
                ) {
                    Text(stringResource(R.string.regresar))
                }
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
