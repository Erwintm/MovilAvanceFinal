package com.example.notas


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.notas.data.Recordatorio
import com.example.notas.viewmodel.RecordatorioViewModel
import java.util.*

@Composable
fun AddRecordatorioScreen(
    navController: NavController,
    noteId: Int,
    recordatorioViewModel: RecordatorioViewModel
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaTexto by remember { mutableStateOf("Seleccionar fecha") }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nuevo Recordatorio", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        // ---- FECHA & HORA ----
        Button(
            onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                calendar.set(year, month, day, hour, minute)
                                fechaTexto = "$day/${month + 1}/$year $hour:$minute"
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                        ).show()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        ) {
            Text(fechaTexto)
        }

        Spacer(Modifier.height(20.dp))

        // ---- GUARDAR ----
        Button(
            onClick = {
                val recordatorio = Recordatorio(
                    titulo = titulo,
                    descripcion = descripcion,
                    fechaRecordatorio = calendar.timeInMillis,
                    notaId = noteId
                )
                recordatorioViewModel.insert(recordatorio)
                navController.popBackStack()
            },
            enabled = titulo.isNotBlank() && fechaTexto != "Seleccionar fecha"
        ) {
            Text("Guardar Recordatorio")
        }

        // ---- CANCELAR ----
        Button(onClick = { navController.popBackStack() }) {
            Text("Cancelar")
        }
    }
}
