package com.example.notas

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notas.ui.theme.TodoappTheme
import com.example.notas.viewmodel.NoteViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSize = calculateWindowSizeClass(this)
            MyApp(windowSize.widthSizeClass)
        }
    }
}

@Composable
fun MyApp(windowSize: WindowWidthSizeClass) {
    val navController = rememberNavController()

    when (windowSize) {
        //Pantalla chica
        WindowWidthSizeClass.Compact -> {

            NavHost(navController = navController, startDestination = "main") {

                composable("main") { MainScreen(navController) }

                composable("add") { AddNote(navController) }

                composable(
                    route = "noteDetail/{noteId}/{title}/{description}/{imageUri}/{idTipo}/{fecha}/{hora}/{estado}",
                    arguments = listOf(
                        navArgument("noteId") { type = NavType.IntType },
                        navArgument("title") { type = NavType.StringType },
                        navArgument("description") { type = NavType.StringType },
                        navArgument("imageUri") { type = NavType.StringType },
                        navArgument("idTipo") { type = NavType.IntType },
                        navArgument("fecha") { type = NavType.StringType },
                        navArgument("hora") { type = NavType.StringType },
                        navArgument("estado") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
                    val title = backStackEntry.arguments?.getString("title") ?: ""
                    val description = backStackEntry.arguments?.getString("description") ?: ""
                    val imageUri =
                        backStackEntry.arguments?.getString("imageUri")?.takeIf { it.isNotEmpty() }
                    val idTipo = backStackEntry.arguments?.getInt("idTipo") ?: 1
                    val fecha =
                        backStackEntry.arguments?.getString("fecha")?.takeIf { it.isNotEmpty() }
                    val hora =
                        backStackEntry.arguments?.getString("hora")?.takeIf { it.isNotEmpty() }
                    val estado =
                        backStackEntry.arguments?.getString("estado")?.takeIf { it.isNotEmpty() }

                    NoteDetailScreen(
                        navController,
                        noteId,
                        title,
                        description,
                        imageUri,
                        idTipo,
                        fecha,
                        hora,
                        estado
                    )
                }

                composable(
                    route = "editNote/{noteId}/{title}/{description}/{imageUri}/{idTipo}/{fecha}/{hora}/{estado}",
                    arguments = listOf(
                        navArgument("noteId") { type = NavType.IntType },
                        navArgument("title") { type = NavType.StringType },
                        navArgument("description") { type = NavType.StringType },
                        navArgument("imageUri") { type = NavType.StringType },
                        navArgument("idTipo") { type = NavType.IntType },
                        navArgument("fecha") { type = NavType.StringType },
                        navArgument("hora") { type = NavType.StringType },
                        navArgument("estado") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
                    val title = backStackEntry.arguments?.getString("title") ?: ""
                    val description = backStackEntry.arguments?.getString("description") ?: ""
                    val imageUri =
                        backStackEntry.arguments?.getString("imageUri")?.takeIf { it.isNotEmpty() }
                    val idTipo = backStackEntry.arguments?.getInt("idTipo") ?: 1
                    val fecha =
                        backStackEntry.arguments?.getString("fecha")?.takeIf { it.isNotEmpty() }
                    val hora =
                        backStackEntry.arguments?.getString("hora")?.takeIf { it.isNotEmpty() }
                    val estado =
                        backStackEntry.arguments?.getString("estado")?.takeIf { it.isNotEmpty() }

                    EditNoteScreen(
                        navController,
                        noteId,
                        title,
                        description,
                        imageUri,
                        idTipo,
                        fecha,
                        hora,
                        estado
                    )
                }
            }
        }
        //Pantalla mediana y grande
        WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                color = Color(0xFF0D1117)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        NavHost(navController = navController, startDestination = "main") {
                            composable("main") { MainScreen(navController) }
                            composable("add") { AddNote(navController) }
                            composable(
                                route = "noteDetail/{noteId}/{title}/{description}/{imageUri}/{idTipo}/{fecha}/{hora}/{estado}",
                                arguments = listOf(
                                    navArgument("noteId") { type = NavType.IntType },
                                    navArgument("title") { type = NavType.StringType },
                                    navArgument("description") { type = NavType.StringType },
                                    navArgument("imageUri") { type = NavType.StringType },
                                    navArgument("idTipo") { type = NavType.IntType },
                                    navArgument("fecha") { type = NavType.StringType },
                                    navArgument("hora") { type = NavType.StringType },
                                    navArgument("estado") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
                                val title = backStackEntry.arguments?.getString("title") ?: ""
                                val description = backStackEntry.arguments?.getString("description") ?: ""
                                val imageUri =
                                    backStackEntry.arguments?.getString("imageUri")?.takeIf { it.isNotEmpty() }
                                val idTipo = backStackEntry.arguments?.getInt("idTipo") ?: 1
                                val fecha =
                                    backStackEntry.arguments?.getString("fecha")?.takeIf { it.isNotEmpty() }
                                val hora =
                                    backStackEntry.arguments?.getString("hora")?.takeIf { it.isNotEmpty() }
                                val estado =
                                    backStackEntry.arguments?.getString("estado")?.takeIf { it.isNotEmpty() }

                                NoteDetailScreen(
                                    navController,
                                    noteId,
                                    title,
                                    description,
                                    imageUri,
                                    idTipo,
                                    fecha,
                                    hora,
                                    estado
                                )
                            }

                            composable(
                                route = "editNote/{noteId}/{title}/{description}/{imageUri}/{idTipo}/{fecha}/{hora}/{estado}",
                                arguments = listOf(
                                    navArgument("noteId") { type = NavType.IntType },
                                    navArgument("title") { type = NavType.StringType },
                                    navArgument("description") { type = NavType.StringType },
                                    navArgument("imageUri") { type = NavType.StringType },
                                    navArgument("idTipo") { type = NavType.IntType },
                                    navArgument("fecha") { type = NavType.StringType },
                                    navArgument("hora") { type = NavType.StringType },
                                    navArgument("estado") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
                                val title = backStackEntry.arguments?.getString("title") ?: ""
                                val description = backStackEntry.arguments?.getString("description") ?: ""
                                val imageUri =
                                    backStackEntry.arguments?.getString("imageUri")?.takeIf { it.isNotEmpty() }
                                val idTipo = backStackEntry.arguments?.getInt("idTipo") ?: 1
                                val fecha =
                                    backStackEntry.arguments?.getString("fecha")?.takeIf { it.isNotEmpty() }
                                val hora =
                                    backStackEntry.arguments?.getString("hora")?.takeIf { it.isNotEmpty() }
                                val estado =
                                    backStackEntry.arguments?.getString("estado")?.takeIf { it.isNotEmpty() }

                                EditNoteScreen(
                                    navController,
                                    noteId,
                                    title,
                                    description,
                                    imageUri,
                                    idTipo,
                                    fecha,
                                    hora,
                                    estado
                                )
                            }
                        }
                    }


                }
            }
        }

    }
}

@Composable
fun MainScreen(navController: androidx.navigation.NavController) {
    val context = LocalContext.current.applicationContext as TodoApplication
    val viewModel = remember { NoteViewModel(context.repository) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val notes by viewModel.getAllNotes().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar", color = Color.White) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { navController.navigate("add") }) { Text("Agregar") }
        }

        Spacer(Modifier.height(16.dp))

        Row {
            listOf("All", "Notes", "Tasks").forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                    RadioButton(selected = selectedFilter == option, onClick = { selectedFilter = option })
                    Text(option, color = Color.White)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(
                notes.filter { note ->
                    val tipoMatches = when (selectedFilter) {
                        "All" -> true
                        "Notes" -> note.idTipo == 1
                        "Tasks" -> note.idTipo == 2
                        else -> true
                    }
                    tipoMatches && note.title.contains(searchQuery, ignoreCase = true)
                }
            ) { note ->
                NoteItemTitle(title = note.title) {
                    val titleEncoded = Uri.encode(note.title)
                    val descEncoded = Uri.encode(note.description)
                    val imgEncoded = note.imageUri?.let { Uri.encode(it) } ?: ""
                    val fecha = note.fechaLimite ?: ""
                    val hora = note.hora ?: ""
                    val estado = note.estado ?: ""
                    navController.navigate("noteDetail/${note.id}/$titleEncoded/$descEncoded/$imgEncoded/${note.idTipo}/$fecha/$hora/$estado")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReplyAppCompactPreview() {
    TodoappTheme {
        Surface { MyApp(WindowWidthSizeClass.Compact) }
    }
}