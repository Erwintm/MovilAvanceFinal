package com.example.notas

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notas.viewmodel.NoteViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {

        // Pantalla principal
        composable("main") {
            MainScreen(navController)
        }

        // Pantalla para agregar una nota
        composable("add") {
            AddNote(navController)
        }

        // Pantalla de detalle
        composable(
            route = "detail/{title}/{description}/{imageUri}"
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val description = backStackEntry.arguments?.getString("description") ?: ""
            val imageUri = backStackEntry.arguments?.getString("imageUri")
            NoteDetailScreen(
                navController = navController,
                initialTitle = title,
                initialDescription = description,
                imageUri = imageUri
            )
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current.applicationContext as TodoApplication
    val viewModel = remember { NoteViewModel(context.repository) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val notes by viewModel.getAllNotes().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(text = "Buscar") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { navController.navigate("add") }) {
                Text("Agregar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(
                notes.filter { note ->
                    (selectedFilter == "All" ||
                            (selectedFilter == "Notes" && !note.title.contains("Tarea", ignoreCase = true)) ||
                            (selectedFilter == "Tasks" && note.title.contains("Tarea", ignoreCase = true))) &&
                            note.title.contains(searchQuery, ignoreCase = true)
                }
            ) { note ->
                NoteItemTitle(
                    title = note.title,
                    onClick = {
                        val titleEncoded = Uri.encode(note.title)
                        val descEncoded = Uri.encode(note.description)
                        val imgEncoded = note.imageUri?.let { Uri.encode(it) } ?: ""
                        navController.navigate("detail/$titleEncoded/$descEncoded/$imgEncoded")
                    }
                )
            }
        }
    }
}