package com.example.notas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavController,
    initialTitle: String,
    initialDescription: String,
    imageUri: String?
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Nota") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Título: $title", style = MaterialTheme.typography.titleLarge)
            Text("Descripción: $description")
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Imagen de la nota",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { /* Aquí puedes agregar función de editar */ }) {
                    Text("Editar")
                }
                Button(onClick = { /* Aquí puedes agregar función de eliminar */ }) {
                    Text("Eliminar")
                }
            }

            Button(onClick = { navController.popBackStack() }) {
                Text("Regresar")
            }
        }
    }
}
