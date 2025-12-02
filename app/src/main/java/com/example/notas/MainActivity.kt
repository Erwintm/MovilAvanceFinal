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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notas.NoteItemTitle
import com.example.notas.data.Note
import com.example.notas.ui.theme.TodoappTheme
import com.example.notas.viewmodel.MainViewModel


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import com.example.notas.alarmas.AlarmSchedulerImpl
import com.example.notas.viewmodel.RecordatorioViewModel
import android.app.AlarmManager
import android.widget.Toast // Importante para mensajes de alerta


// ----------------------------------------------------------------------------------


class MainActivity : ComponentActivity() {


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel", // mismo ID que uses en AlarmReceiver
                "Alarmas y Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para recordatorios exactos"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private val PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.POST_NOTIFICATIONS
    )
    private val REQUEST_CODE_PERMISSIONS = 100

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // **CLAVE:** Llamamos a la solicitud de permisos cada vez que se crea la actividad.
        // Android mostrará el diálogo solo si los permisos aún no están concedidos.
        requestCriticalPermissions()
        createNotificationChannel()
        requestExactAlarmPermission()
        enableEdgeToEdge()

        // detectar si se abrió desde la notificación
        val noteId = intent.getIntExtra("NOTE_ID", -1)
        val fromNotification = intent.getBooleanExtra("FROM_NOTIFICATION", false)

        setContent {
            TodoappTheme {
                val windowSize = calculateWindowSizeClass(this)

                // pasar la info a tu App
                MyApp(
                    windowSize = windowSize.widthSizeClass,
                    noteIdToOpen = if (fromNotification) noteId else null
                )
            }
        }
    }


    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("AlarmScheduler", "No se pudo solicitar permiso de exact alarm: ${e.message}")
                }
            }
        }
    }

    /**
     * Función que abre la pantalla de Configuración de la aplicación
     * para que el usuario active los permisos manualmente.
     */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    /**
     * Función que fuerza la solicitud de los cuadros de diálogo de permisos
     * cada vez que se llama, sin importar si el usuario ya los había denegado antes.
     */
    private fun requestCriticalPermissions() {
        // Solicitamos los permisos directamente.
        // Android gestiona si debe mostrar el diálogo o no
        // (si ya están concedidos, no se muestran).
        ActivityCompat.requestPermissions(
            this,
            PERMISSIONS,
            REQUEST_CODE_PERMISSIONS
        )
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            var permissionDenied = false
            // Revisamos los resultados de la solicitud que acaba de ocurrir
            for (result in grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    permissionDenied = true
                    break
                }
            }

            if (permissionDenied) {
                // Si el usuario denegó *cualquier* permiso en el diálogo, lo redirigimos a Ajustes.
                // Esto es crucial si deniega y marca "No volver a preguntar".
                Toast.makeText(this, "Permiso denegado. Active los permisos en Ajustes.", Toast.LENGTH_LONG).show()
                openAppSettings()
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyApp(windowSize: WindowWidthSizeClass,noteIdToOpen: Int?) {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext as TodoApplication


    val mainViewModel: MainViewModel = viewModel(factory = NoteViewModelFactory(context.repository))

    when (windowSize) {
        // Pantalla chica
        WindowWidthSizeClass.Compact -> {
            NavHost(navController = navController, startDestination = "main") {
                //Navegar por la notificación
                composable("main") {
                    MainScreen(navController, mainViewModel)

                    if (noteIdToOpen != null && noteIdToOpen != -1) {
                        LaunchedEffect(Unit) {
                            navController.navigate("openNote/$noteIdToOpen")
                        }
                    }
                }
                composable("main") { MainScreen(navController, mainViewModel) }

                composable("add") { AddNote(navController) }


                composable(
                    // RUTA CORREGIDA: Eliminado {imageUri}
                    route = "noteDetail/{noteId}/{title}/{description}/{idTipo}/{fecha}/{hora}/{estado}",
                    arguments = listOf(
                        navArgument("noteId") { type = NavType.IntType },
                        navArgument("title") { type = NavType.StringType },
                        navArgument("description") { type = NavType.StringType },
                        // ELIMINADO: navArgument("imageUri")
                        navArgument("idTipo") { type = NavType.IntType },
                        navArgument("fecha") { type = NavType.StringType; nullable = true },
                        navArgument("hora") { type = NavType.StringType; nullable = true },
                        navArgument("estado") { type = NavType.StringType; nullable = true }
                    )
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
                    val title = backStackEntry.arguments?.getString("title") ?: ""
                    val description = backStackEntry.arguments?.getString("description") ?: ""
                    // ELIMINADO: val imageUri = backStackEntry.arguments?.getString("imageUri")
                    val idTipo = backStackEntry.arguments?.getInt("idTipo") ?: 1
                    val fecha = backStackEntry.arguments?.getString("fecha")
                    val hora = backStackEntry.arguments?.getString("hora")
                    val estado = backStackEntry.arguments?.getString("estado")

                    // LLAMADA CORREGIDA: Eliminado imageUri
                    NoteDetailScreen(
                        navController,
                        noteId,
                        title,
                        description,
                        idTipo = idTipo,
                        fechaLimite = fecha,
                        hora = hora,
                        estado = estado
                    )
                }

                composable(
                    // RUTA CORREGIDA: Eliminado {imageUri}
                    route = "editNote/{noteId}/{title}/{description}/{idTipo}/{fecha}/{hora}/{estado}",
                    arguments = listOf(
                        navArgument("noteId") { type = NavType.IntType },
                        navArgument("title") { type = NavType.StringType },
                        navArgument("description") { type = NavType.StringType },
                        // ELIMINADO: navArgument("imageUri")
                        navArgument("idTipo") { type = NavType.IntType },
                        navArgument("fecha") { type = NavType.StringType; nullable = true },
                        navArgument("hora") { type = NavType.StringType; nullable = true },
                        navArgument("estado") { type = NavType.StringType; nullable = true }
                    )
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
                    val title = backStackEntry.arguments?.getString("title") ?: ""
                    val description = backStackEntry.arguments?.getString("description") ?: ""
                    // ELIMINADO: val imageUri = backStackEntry.arguments?.getString("imageUri")
                    val idTipo = backStackEntry.arguments?.getInt("idTipo") ?: 1
                    val fecha =
                        backStackEntry.arguments?.getString("fecha")?.takeIf { it.isNotEmpty() }
                    val hora =
                        backStackEntry.arguments?.getString("hora")?.takeIf { it.isNotEmpty() }
                    val estado =
                        backStackEntry.arguments?.getString("estado")?.takeIf { it.isNotEmpty() }

                    // LLAMADA CORREGIDA: Eliminado imageUri
                    EditNoteScreen(
                        navController,
                        noteId,
                        title,
                        description,
                        idTipo = idTipo,
                        fechaLimiteInit = fecha,
                        horaInit = hora,
                        estadoInit = estado
                    )
                }

                //  LISTA DE RECORDATORIOS
                composable(
                    route = "recordatorios/{noteId}",
                    arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                ) { backStackEntry ->

                    val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0

                    val app = LocalContext.current.applicationContext as TodoApplication
                    val recordatorioViewModel: RecordatorioViewModel =
                        viewModel(factory = RecordatorioViewModelFactory(app.recordatorioRepository))

                    RecordatoriosListScreen(
                        navController = navController,
                        noteId = noteId,
                        recordatorioViewModel = recordatorioViewModel
                    )
                }

                // AGREGAR RECORDATORIO
                composable(
                    route = "addRecordatorio/{noteId}",
                    arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                ) { backStackEntry ->

                    val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0

                    val app = LocalContext.current.applicationContext as TodoApplication
                    val recordatorioViewModel: RecordatorioViewModel =
                        viewModel(factory = RecordatorioViewModelFactory(app.recordatorioRepository))

                    AddRecordatorioScreen(
                        navController = navController,
                        noteId = noteId,
                        recordatorioViewModel = recordatorioViewModel,
                        alarmScheduler = AlarmSchedulerImpl(LocalContext.current)
                    )
                }

                // EDITAR RECORDATORIO
                composable(
                    route = "editRecordatorio/{recordatorioId}",
                    arguments = listOf(navArgument("recordatorioId") { type = NavType.IntType })
                ) { backStackEntry ->

                    val recordatorioId = backStackEntry.arguments?.getInt("recordatorioId") ?: 0

                    val app = LocalContext.current.applicationContext as TodoApplication
                    val recordatorioViewModel: RecordatorioViewModel =
                        viewModel(factory = RecordatorioViewModelFactory(app.recordatorioRepository))

                    EditRecordatorioScreen(
                        navController = navController,
                        recordatorioId = recordatorioId,
                        recordatorioViewModel = recordatorioViewModel,
                        alarmScheduler = AlarmSchedulerImpl(LocalContext.current)
                    )
                }




            }
        }
        // Pantalla mediana y grande
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
                            //Navegar por la notificación
                            composable("main") {
                                MainScreen(navController, mainViewModel)

                                if (noteIdToOpen != null && noteIdToOpen != -1) {
                                    LaunchedEffect(Unit) {
                                        navController.navigate("openNote/$noteIdToOpen")
                                    }
                                }
                            }
                            // 2. MainScreen recibe el ViewModel
                            composable("main") { MainScreen(navController, mainViewModel) }
                            composable("add") { AddNote(navController) }

                            composable(
                                // RUTA CORREGIDA: Eliminado {imageUri}
                                route = "noteDetail/{noteId}/{title}/{description}/{idTipo}/{fecha}/{hora}/{estado}",
                                arguments = listOf(
                                    navArgument("noteId") { type = NavType.IntType },
                                    navArgument("title") { type = NavType.StringType },
                                    navArgument("description") { type = NavType.StringType },
                                    // ELIMINADO: navArgument("imageUri")
                                    navArgument("idTipo") { type = NavType.IntType },
                                    navArgument("fecha") { type = NavType.StringType; nullable = true },
                                    navArgument("hora") { type = NavType.StringType; nullable = true },
                                    navArgument("estado") { type = NavType.StringType; nullable = true }
                                )
                            ) { backStackEntry ->
                                val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
                                val title = backStackEntry.arguments?.getString("title") ?: ""
                                val description = backStackEntry.arguments?.getString("description") ?: ""
                                // ELIMINADO: val imageUri = backStackEntry.arguments?.getString("imageUri")
                                val idTipo = backStackEntry.arguments?.getInt("idTipo") ?: 1
                                val fecha =
                                    backStackEntry.arguments?.getString("fecha")?.takeIf { it.isNotEmpty() }
                                val hora =
                                    backStackEntry.arguments?.getString("hora")?.takeIf { it.isNotEmpty() }
                                val estado =
                                    backStackEntry.arguments?.getString("estado")?.takeIf { it.isNotEmpty() }

                                // LLAMADA CORREGIDA: Eliminado imageUri
                                NoteDetailScreen(
                                    navController,
                                    noteId,
                                    title,
                                    description,
                                    idTipo = idTipo,
                                    fechaLimite = fecha,
                                    hora = hora,
                                    estado = estado
                                )
                            }

                            composable(
                                // RUTA CORREGIDA: Eliminado {imageUri}
                                route = "editNote/{noteId}/{title}/{description}/{idTipo}/{fecha}/{hora}/{estado}",
                                arguments = listOf(
                                    navArgument("noteId") { type = NavType.IntType },
                                    navArgument("title") { type = NavType.StringType },
                                    navArgument("description") { type = NavType.StringType },
                                    // ELIMINADO: navArgument("imageUri")
                                    navArgument("idTipo") { type = NavType.IntType },
                                    navArgument("fecha") { type = NavType.StringType; nullable = true },
                                    navArgument("hora") { type = NavType.StringType; nullable = true },
                                    navArgument("estado") { type = NavType.StringType; nullable = true }
                                )
                            ) { backStackEntry ->
                                val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
                                val title = backStackEntry.arguments?.getString("title") ?: ""
                                val description = backStackEntry.arguments?.getString("description") ?: ""
                                // ELIMINADO: val imageUri = backStackEntry.arguments?.getString("imageUri")
                                val idTipo = backStackEntry.arguments?.getInt("idTipo") ?: 1
                                val fecha =
                                    backStackEntry.arguments?.getString("fecha")?.takeIf { it.isNotEmpty() }
                                val hora =
                                    backStackEntry.arguments?.getString("hora")?.takeIf { it.isNotEmpty() }
                                val estado =
                                    backStackEntry.arguments?.getString("estado")?.takeIf { it.isNotEmpty() }

                                // LLAMADA CORREGIDA: Eliminado imageUri
                                EditNoteScreen(
                                    navController,
                                    noteId,
                                    title,
                                    description,
                                    idTipo = idTipo,
                                    fechaLimiteInit = fecha,
                                    horaInit = hora,
                                    estadoInit = estado
                                )
                            }
                            //  LISTA DE RECORDATORIOS
                            composable(
                                route = "recordatorios/{noteId}",
                                arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                            ) { backStackEntry ->

                                val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0

                                val app = LocalContext.current.applicationContext as TodoApplication
                                val recordatorioViewModel: RecordatorioViewModel =
                                    viewModel(factory = RecordatorioViewModelFactory(app.recordatorioRepository))

                                RecordatoriosListScreen(
                                    navController = navController,
                                    noteId = noteId,
                                    recordatorioViewModel = recordatorioViewModel
                                )
                            }

                            // AGREGAR RECORDATORIO
                            composable(
                                route = "addRecordatorio/{noteId}",
                                arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                            ) { backStackEntry ->

                                val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0

                                val app = LocalContext.current.applicationContext as TodoApplication
                                val recordatorioViewModel: RecordatorioViewModel =
                                    viewModel(factory = RecordatorioViewModelFactory(app.recordatorioRepository))

                                AddRecordatorioScreen(
                                    navController = navController,
                                    noteId = noteId,
                                    recordatorioViewModel = recordatorioViewModel,
                                    alarmScheduler = AlarmSchedulerImpl(LocalContext.current)
                                )
                            }

                            // EDITAR RECORDATORIO
                            composable(
                                route = "editRecordatorio/{recordatorioId}",
                                arguments = listOf(navArgument("recordatorioId") { type = NavType.IntType })
                            ) { backStackEntry ->

                                val recordatorioId = backStackEntry.arguments?.getInt("recordatorioId") ?: 0

                                val app = LocalContext.current.applicationContext as TodoApplication
                                val recordatorioViewModel: RecordatorioViewModel =
                                    viewModel(factory = RecordatorioViewModelFactory(app.recordatorioRepository))

                                EditRecordatorioScreen(
                                    navController = navController,
                                    recordatorioId = recordatorioId,
                                    recordatorioViewModel = recordatorioViewModel,
                                    alarmScheduler = AlarmSchedulerImpl(LocalContext.current)
                                )
                            }

                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF161B22))
                            .padding(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.detalles_tareas),
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )

                        val notes by mainViewModel.filteredNotes.collectAsState()

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(notes) { note ->
                                NoteSummary(note)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---

@Composable
fun MainScreen(
    navController: androidx.navigation.NavController,
    viewModel: MainViewModel
) {
//Escuchan al viewmodel y traen los datos
    val searchQuery by viewModel.searchText.collectAsState()
    val notes by viewModel.filteredNotes.collectAsState()
    val selectedFilterId by viewModel.filterType.collectAsState()

    val filterOptions = listOf(
        Pair(stringResource(R.string.todas), 0),
        Pair(stringResource(R.string.notas), 1),
        Pair(stringResource(R.string.tareas), 2)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = searchQuery,
                //lama al viewmodel para actualizar la ui
                onValueChange = viewModel::updateSearchText,
                label = { Text(stringResource(R.string.buscar), color = Color.White) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { navController.navigate("add") }) { Text(stringResource(R.string.agregar)) }
        }

        Spacer(Modifier.height(16.dp))


        Row {
            filterOptions.forEach { (optionText, optionId) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                    RadioButton(
                        selected = selectedFilterId == optionId,
                        // 3. Handler del ViewModel
                        onClick = { viewModel.updateFilterType(optionId) }
                    )
                    Text(optionText, color = Color.White)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn {

            items(notes) { note ->
                NoteItemTitle(title = note.title) {
                    val titleEncoded = Uri.encode(note.title)
                    val descEncoded = Uri.encode(note.description)
                    // ELIMINADO: val imgEncoded = Uri.encode(note.imageUri ?: "")

                    // Codificar SIEMPRE los campos opcionales.
                    val fechaEncoded = Uri.encode(note.fechaLimite ?: "")
                    val horaEncoded = Uri.encode(note.hora ?: "")
                    val estadoEncoded = Uri.encode(note.estado ?: "")

                    // RUTA CORREGIDA: Eliminado $imgEncoded
                    navController.navigate("noteDetail/${note.id}/$titleEncoded/$descEncoded/${note.idTipo}/$fechaEncoded/$horaEncoded/$estadoEncoded")
                }
            }
        }
    }
}
@Composable
fun NoteSummary(note: Note) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161B22))
            .padding(12.dp)
    ) {
        Text("${stringResource(R.string.titulo)}: ${note.title}", color = Color.White, style = MaterialTheme.typography.titleMedium)
        Text("${stringResource(R.string.descripcion)}: ${note.description}", color = Color.LightGray)
        if (note.idTipo == 2) {
            Text("${stringResource(R.string.fecha_límite)}: ${note.fechaLimite ?: "-"}", color = Color.Gray)
            Text("${stringResource(R.string.hora)}: ${note.hora ?: "-"}", color = Color.Gray)
            Text("${stringResource(R.string.estado)}: ${note.estado ?: "Pendiente"}", color = Color.Gray)
        }
    }
}