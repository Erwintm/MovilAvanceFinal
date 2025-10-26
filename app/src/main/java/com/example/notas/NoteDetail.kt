package com.example.notas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import androidx.navigation.NavController
import com.example.notas.data.Note
import com.example.notas.viewmodel.NoteViewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavController,
    noteId: Int,
    initialTitle: String,
    initialDescription: String,
    imageUri: String? = null,
    idTipo: Int = 1,
    fechaLimite: String? = null,
    hora: String? = null,
    estado: String? = null
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }

    val context = LocalContext.current.applicationContext as TodoApplication
    val viewModel = remember { NoteViewModel(context.repository) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (idTipo == 1) "Detalle de Nota" else "Detalle de Tarea") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Título: $title", style = MaterialTheme.typography.titleLarge)
            Text("Descripción: $description")

            if (idTipo == 2) {
                Text("Fecha límite: ${fechaLimite ?: "-"}")
                Text("Hora: ${hora ?: "-"}")
                Text("Estado: ${estado ?: "Pendiente"}")
            }

            if (!imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Imagen",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Row con los 3 botones
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        navController.navigate(
                            "editNote/$noteId/$title/$description/${imageUri ?: ""}/$idTipo/${fechaLimite ?: ""}/${hora ?: ""}/${estado ?: ""}"
                        )
                    }
                ) { Text("Editar") }

                Button(
                    onClick = {
                        viewModel.delete(
                            Note(
                                id = noteId,
                                title = title,
                                description = description,
                                imageUri = imageUri,
                                idTipo = idTipo,
                                fechaLimite = fechaLimite,
                                hora = hora,
                                estado = estado
                            )
                        )
                        navController.popBackStack("main", inclusive = false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.onError)
                }

                Button(onClick = { navController.popBackStack("main", inclusive = false) }) {
                    Text("Regresar")
                }
            }
        }
    }
}

