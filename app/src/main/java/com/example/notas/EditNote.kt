package com.example.notas

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.notas.data.Note
import com.example.notas.viewmodel.NoteViewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    navController: NavController,
    noteId: Int,
    initialTitle: String,
    initialDescription: String,
    imageUri: String?,
    idTipo: Int = 1,
    fechaLimiteInit: String? = null,
    horaInit: String? = null,
    estadoInit: String? = null
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var fechaLimite by remember { mutableStateOf(fechaLimiteInit ?: "") }
    var hora by remember { mutableStateOf(horaInit ?: "") }
    var estado by remember { mutableStateOf(estadoInit ?: "Pendiente") }

    val context = LocalContext.current.applicationContext as TodoApplication
    val viewModel = remember { NoteViewModel(context.repository) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Nota/Tarea", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = Color.White)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = Color.White)
            )

            if (idTipo == 2) {
                OutlinedTextField(
                    value = fechaLimite,
                    onValueChange = { fechaLimite = it },
                    label = { Text("Fecha límite", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )

                OutlinedTextField(
                    value = hora,
                    onValueChange = { hora = it },
                    label = { Text("Hora", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )

                OutlinedTextField(
                    value = estado,
                    onValueChange = { estado = it },
                    label = { Text("Estado", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )
            }

            Button(
                onClick = {
                    val updated = Note(
                        id = noteId,
                        title = title.ifBlank { "(sin título)" },
                        description = description,
                        imageUri = imageUri,
                        idTipo = idTipo,
                        fechaLimite = if (idTipo == 2) fechaLimite.ifBlank { null } else null,
                        hora = if (idTipo == 2) hora.ifBlank { null } else null,
                        estado = if (idTipo == 2) estado.ifBlank { "Pendiente" } else "Pendiente"
                    )
                    viewModel.update(updated)

                    val titleEncoded = Uri.encode(updated.title)
                    val descEncoded = Uri.encode(updated.description)
                    val imgEncoded = updated.imageUri?.let { Uri.encode(it) } ?: ""
                    val fechaEncoded = updated.fechaLimite?.let { Uri.encode(it) } ?: ""
                    val horaEncoded = updated.hora?.let { Uri.encode(it) } ?: ""
                    val estadoEncoded = updated.estado?.let { Uri.encode(it) } ?: "Pendiente"

                    navController.popBackStack()
                    navController.navigate("noteDetail/${updated.id}/$titleEncoded/$descEncoded/$imgEncoded/$idTipo/$fechaEncoded/$horaEncoded/$estadoEncoded")
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar cambios") }

            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }
}
