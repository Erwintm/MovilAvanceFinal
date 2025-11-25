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
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.autofill.ContentDataType.Companion.Date
import androidx.core.content.ContextCompat
import com.example.notas.utils.AudioRecorder
import com.example.notas.data.Multimedia
import com.example.notas.utils.AudioPlayer // <-- ¡IMPORTADO!
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player // <-- ¡IMPORTADO!
import com.example.notas.viewmodel.RecordatorioViewModel
import java.util.Date
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import com.example.notas.data.Recordatorio


// ----------------------------------------------------------------------
// NUEVO COMPOSABLE: Componente de Control del Reproductor de Audio
// ----------------------------------------------------------------------
@Composable
fun AudioPlayerControl(
    fileName: String,
    player: AudioPlayer
) {
    // Estado para rastrear si el audio está reproduciéndose
    var isPlaying by remember { mutableStateOf(false) }

    // Efecto para escuchar cambios de reproducción (si el audio termina, actualiza isPlaying)
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(currentIsPlaying: Boolean) {
                isPlaying = currentIsPlaying
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                }
            }
        }
        player.player?.addListener(listener)

        // Liberar el reproductor cuando este composable salga de la composición
        onDispose {
            player.player?.removeListener(listener)
            player.release()
        }
    }

    Button(
        onClick = {
            if (isPlaying) {
                player.stop()
            } else {
                player.play(fileName)
            }
        },
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(if (isPlaying) "Stop" else "Play")
    }
}
// ----------------------------------------------------------------------

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
    val recordatorioViewModel: RecordatorioViewModel = viewModel(
        factory = RecordatorioViewModelFactory(context.recordatorioRepository)
    )

    LaunchedEffect(key1 = noteId) {
        viewModel.initialize(noteId)
    }

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

    // ----------------------------------------------------------------------
    // LÓGICA DE GRABACIÓN Y REPRODUCCIÓN (Unidad 8)
    // ----------------------------------------------------------------------

    val recorder = remember { AudioRecorder(context) }
    val audioPlayer = remember { AudioPlayer(context) } // <-- ¡INSTANCIA DEL REPRODUCTOR!
    var isRecording by remember { mutableStateOf(false) }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val fileName = "audio_${System.currentTimeMillis()}.mp4"
            recorder.start(fileName)
            isRecording = true
        } else {
            // Permiso Denegado
        }
    }

    val startRecordingFlow: () -> Unit = {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                val fileName = "audio_${System.currentTimeMillis()}.mp4"
                recorder.start(fileName)
                isRecording = true
            }
            else -> {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    val stopRecordingFlow: () -> Unit = {
        val fileName = recorder.stop()
        isRecording = false
        if (fileName != null) {
            val newMultimedia = Multimedia(
                notaId = noteId,
                uriArchivo = fileName,
                tipo = "AUDIO"
            )
            viewModel.insertMultimedia(newMultimedia)
        }
    }

    // Lista observable de archivos multimedia
    val multimediaList by viewModel.multimediaList.collectAsState()
    val recordatorios by recordatorioViewModel
        .getRecordatoriosByNota(noteId)
        .collectAsState(initial = emptyList())

    Button(onClick = {
        recordatorioViewModel.insert(
            Recordatorio(
                titulo = "Nuevo recordatorio",
                descripcion = "",
                fechaRecordatorio = System.currentTimeMillis() + 3600000,
                notaId = noteId
            )
        )
    }) {
        Text("Agregar recordatorio")
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        "Recordatorios (${recordatorios.size}):",
        color = Color.White,
        fontSize = 18.sp
    )

    Divider(color = Color.Gray)

    if (recordatorios.isEmpty()) {
        Text(
            "No hay recordatorios para esta nota.",
            color = Color.Gray,
            modifier = Modifier.padding(top = 6.dp)
        )
    } else {
        recordatorios.forEach { r ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text("Título: ${r.titulo}", color = Color.White)
                Text("Descripción: ${r.descripcion}", color = Color.LightGray)
                Text("Fecha: ${Date(r.fechaRecordatorio)}", color = Color.Gray)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { recordatorioViewModel.delete(r) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }

    // ----------------------------------------------------------------------

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

            // Botón de Grabación
            Button(
                onClick = {
                    if (isRecording) stopRecordingFlow() else startRecordingFlow()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color.Red else Color.Green
                )
            ) {
                Text(if (isRecording) "Detener Grabación" else "Grabar Audio")
            }

            // ----------------------------------------------------------------------
            // VISUALIZACIÓN DE MULTIMEDIA (CORREGIDO PARA EVITAR CRASH)
            // ----------------------------------------------------------------------

            Spacer(Modifier.height(10.dp))
            Text("Archivos Multimedia Asociados (${multimediaList.size}):", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

            if (multimediaList.isEmpty()) {
                Text("No hay archivos multimedia adjuntos.", color = Color.Gray)
            } else {
                multimediaList.forEach { multimedia ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${multimedia.tipo}: ${multimedia.uriArchivo}",
                            color = Color.LightGray,
                            modifier = Modifier.weight(1f)
                        )

                        // 💥 CORRECCIÓN CRÍTICA: Solo mostrar el control de reproducción si es AUDIO
                        if (multimedia.tipo == "AUDIO") {
                            AudioPlayerControl(
                                fileName = multimedia.uriArchivo,
                                player = audioPlayer
                            )
                        }
                        // Opcional: Agregar lógica para tipo "IMAGEN" si es necesario

                        IconButton(
                            onClick = {
                                viewModel.deleteMultimedia(multimedia, context.filesDir)
                            }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = android.R.drawable.ic_delete),
                                contentDescription = "Eliminar",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }

            // Contenido Antiguo (Imagen simple)
            if (!currentNote.imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = currentNote.imageUri,
                    contentDescription = "Imagen Antigua",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ... (Row de botones Editar, Eliminar, Regresar)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ... (Botones Editar, Eliminar, Regresar)
                Button(
                    onClick = {
                        val titleEncoded = Uri.encode(currentNote.title)
                        val descEncoded = Uri.encode(currentNote.description)
                        val imgEncoded = currentNote.imageUri?.let { Uri.encode(it) } ?: Uri.encode("")
                        val fechaEncoded = Uri.encode(currentNote.fechaLimite ?: "")
                        val horaEncoded = Uri.encode(currentNote.hora ?: "")
                        val estadoEncoded = Uri.encode(currentNote.estado ?: "")

                        navController.navigate(
                            "editNote/${currentNote.id}/$titleEncoded/$descEncoded/$imgEncoded/${currentNote.idTipo}/$fechaEncoded/$horaEncoded/$estadoEncoded"
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

                Button(onClick = { navController.popBackStack("main", inclusive = false) }) {
                    Text(stringResource(R.string.regresar))
                }
            }
        }
    }
}