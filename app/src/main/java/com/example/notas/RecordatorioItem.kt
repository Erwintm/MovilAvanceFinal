package com.example.notas

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType.Companion.Date
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.notas.data.Recordatorio
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordatorioItem(
    recordatorio: Recordatorio,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    // Convertir epochMillis a LocalDateTime
    val dateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(recordatorio.fechaRecordatorio),
        ZoneId.systemDefault()
    )


    val fechaTexto = dateTime.format(
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = recordatorio.titulo, style = MaterialTheme.typography.titleMedium)
        Text(text = recordatorio.descripcion)

        Spacer(modifier = Modifier.height(4.dp))


        Text(
            text = fechaTexto,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}
