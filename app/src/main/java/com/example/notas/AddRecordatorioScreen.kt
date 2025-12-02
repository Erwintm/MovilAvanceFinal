package com.example.notas


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.notas.alarmas.AlarmItem
import com.example.notas.alarmas.AlarmScheduler
import com.example.notas.alarmas.AlarmSchedulerImpl
import com.example.notas.data.Recordatorio
import com.example.notas.viewmodel.RecordatorioViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddRecordatorioScreen(
    navController: NavController,
    noteId: Int,
    recordatorioViewModel: RecordatorioViewModel,
    alarmScheduler: AlarmScheduler
) {
    val context = LocalContext.current

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaTexto by remember { mutableStateOf("Seleccionar fecha") }

    // Calendar donde se guardará la fecha correcta
    var fechaRecordatorioMillis by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.nuevo_Recordatorio), style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text(stringResource(R.string.titulo)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text(stringResource(R.string.descripcion)) },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val calendar = Calendar.getInstance()

                DatePickerDialog(
                    context,
                    { _, year, month, day ->

                        TimePickerDialog(
                            context,
                            { _, hour, minute ->

                                // guardar fecha y hora
                                calendar.set(Calendar.YEAR, year)
                                calendar.set(Calendar.MONTH, month)
                                calendar.set(Calendar.DAY_OF_MONTH, day)
                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                calendar.set(Calendar.MINUTE, minute)
                                calendar.set(Calendar.SECOND, 0)
                                calendar.set(Calendar.MILLISECOND, 0)

                                fechaRecordatorioMillis = calendar.timeInMillis

                                val dateTime = LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(calendar.timeInMillis),
                                    ZoneId.systemDefault()
                                )

                                fechaTexto = dateTime.format(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                                )
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

        // GUARDAR
        Button(
            onClick = {

                val fechaFinal = fechaRecordatorioMillis!!

                // DB
                val recordatorio = Recordatorio(
                    titulo = titulo,
                    descripcion = descripcion,
                    fechaRecordatorio = fechaFinal,
                    notaId = noteId
                )

                recordatorioViewModel.insert(recordatorio)

                // ALARM
                val dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(fechaFinal),
                    ZoneId.systemDefault()
                )

                alarmScheduler.schedule(
                    AlarmItem(
                        noteId = noteId,
                        alarmTime = dateTime,
                        title = titulo,
                        description = descripcion
                    )
                )

                navController.popBackStack()
            },
            enabled = titulo.isNotBlank() && fechaRecordatorioMillis != null
        ) {
            Text(stringResource(R.string.guardar_Recordatorio))
        }

        Button(onClick = { navController.popBackStack() }) {
            Text(stringResource(R.string.cancelar))
        }
    }
}



