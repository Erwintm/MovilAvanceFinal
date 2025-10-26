package com.example.notas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    imageUri: String?
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }

    val context = LocalContext.current.applicationContext as TodoApplication
    val viewModel = remember { NoteViewModel(context.repository) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Nota") }) }
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
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    // Crear el objeto actualizado
                    val updatedNote = Note(
                        id = noteId,
                        title = title,
                        description = description,
                        imageUri = imageUri
                    )

                    // Actualizar en base de datos
                    viewModel.updateNote(updatedNote)

                    // Regresar al detalle con los valores nuevos
                    navController.navigate(
                        "noteDetail/${updatedNote.id}/${updatedNote.title}/${updatedNote.description}/${updatedNote.imageUri ?: ""}"
                    ) {
                        popUpTo("noteList") { inclusive = false }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }

            Button(onClick = { navController.popBackStack() }) {
                Text("Cancelar")
            }
        }
    }
}
