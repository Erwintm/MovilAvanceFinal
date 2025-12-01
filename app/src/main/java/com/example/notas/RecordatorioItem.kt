package com.example.notas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType.Companion.Date
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.notas.data.Recordatorio
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordatorioItem(
    item: Recordatorio,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {

            Text(item.titulo, style = MaterialTheme.typography.titleMedium)
            Text(item.descripcion)

            Text(
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(Date(item.fechaRecordatorio)),
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onEdit) {
                    Text(stringResource(R.string.editar))
                }
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.eliminar))
                }
            }
        }
    }
}
