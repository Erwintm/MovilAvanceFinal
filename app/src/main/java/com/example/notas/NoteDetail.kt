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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notas.viewmodel.NoteDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavController,
    noteId: Int,
    title: String,
    description: String,
    imageUri: String? = null,
    idTipo: Int = 1,
    fechaLimite: String? = null,
    hora: String? = null,
    estado: String? = null
) {


    val context = LocalContext.current.applicationContext as TodoApplication


    val viewModel: NoteDetailViewModel = viewModel(
        factory = NoteViewModelFactory(context.repository)
    )

//econstrucción del Objeto Note (Estado)
    val currentNote = remember {
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
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (idTipo == 1) "Detalle de Nota" else "Detalle de Tarea",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("${stringResource(R.string.titulo)}: ${currentNote.title}", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text("Descripción: ${currentNote.description}", color = Color.White)

            if (idTipo == 2) {
                Text("${stringResource(R.string.fecha_límite)}: ${currentNote.fechaLimite ?: "-"}", color = Color.White)
                Text("${stringResource(R.string.hora)}: ${currentNote.hora ?: "-"}", color = Color.White)
                Text("${stringResource(R.string.estado)}: ${currentNote.estado ?: "Pendiente"}", color = Color.White)
            }

            if (!currentNote.imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = currentNote.imageUri,
                    contentDescription = "Imagen",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(
                    onClick = {
                    // Navega a la ruta de edición, volviendo a pasar TODOS los dato
                        navController.navigate(
                            "editNote/${currentNote.id}/${currentNote.title}/${currentNote.description}/${currentNote.imageUri ?: ""}/${currentNote.idTipo}/${currentNote.fechaLimite ?: ""}/${currentNote.hora ?: ""}/${currentNote.estado ?: ""}"
                        )
                    }
                ) { Text(stringResource(R.string.editar)) }


                Button(
                    onClick = {

                        viewModel.deleteNote(currentNote)

                        navController.popBackStack("main", inclusive = false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                ) {
                    Text(stringResource(R.string.eliminar), color = MaterialTheme.colorScheme.onError)
                }

                // Botón Regresar
                Button(onClick = { navController.popBackStack("main", inclusive = false) }) {
                    Text(stringResource(R.string.regresar))
                }
            }
        }
    }
}