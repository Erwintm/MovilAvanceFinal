package com.example.notas

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.notas.alarmas.AlarmItem
import com.example.notas.alarmas.AlarmScheduler
import com.example.notas.viewmodel.RecordatorioViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.ui.res.stringResource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditRecordatorioScreen(
    navController: NavController,
    recordatorioId: Int,
    recordatorioViewModel: RecordatorioViewModel,
    alarmScheduler: AlarmScheduler
) {
    val context = LocalContext.current

    val recordatorio = recordatorioViewModel
        .getRecordatorioById(recordatorioId)
        .collectAsState(initial = null).value

    if (recordatorio == null) {
        Text("Cargando...")
        return
    }

    var titulo by remember { mutableStateOf(recordatorio.titulo) }
    var descripcion by remember { mutableStateOf(recordatorio.descripcion) }

    val calendar = remember {
        Calendar.getInstance().apply {
            timeInMillis = recordatorio.fechaRecordatorio
        }
    }

    var fechaTexto by remember {
        mutableStateOf(
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date(recordatorio.fechaRecordatorio))
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.editar_recordatorio), style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.titulo)) }
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.descripcion)) }
        )

        // Selección de fecha/hora
        Button(onClick = {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            calendar.set(year, month, day, hour, minute)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
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
        }) {
            Text(fechaTexto)
        }

        // Guardar cambios
        Button(onClick = {
            val actualizado = recordatorio.copy(
                titulo = titulo,
                descripcion = descripcion,
                fechaRecordatorio = calendar.timeInMillis
            )

            // Update en BD
            recordatorioViewModel.update(actualizado)

            // Reprogramar alarma
            val dateTime = Instant.ofEpochMilli(actualizado.fechaRecordatorio)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            alarmScheduler.schedule(
                AlarmItem(
                    noteId = actualizado.notaId,
                    alarmTime = dateTime,
                    title = actualizado.titulo,
                    description = actualizado.descripcion
                )
            )

            navController.popBackStack()
        }) {
            Text(stringResource(R.string.guardar_cambios))
        }
    }
}


