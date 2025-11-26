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
import androidx.compose.ui.platform.LocalContext // <-- Importado
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.core.content.ContextCompat
import com.example.notas.utils.AudioRecorder
import com.example.notas.data.Multimedia
import com.example.notas.utils.AudioPlayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.example.notas.data.Recordatorio
import com.example.notas.viewmodel.RecordatorioViewModel
import java.util.Date

// ----------------------------------------------------------------------
// COMPOSABLE: Componente de Control del Reproductor de Audio
// ----------------------------------------------------------------------
@Composable
fun AudioPlayerControl(
    fileName: String,
    player: AudioPlayer
) {
    var isPlaying by remember { mutableStateOf(false) }

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

        onDispose {
            player.player?.removeListener(listener)
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


    // 🚨 CORRECCIÓN CLAVE (PLAN B): Usamos el contexto de la Activity (LocalContext.current)
    val activityContext = LocalContext.current

    // Obtenemos el repositorio del contexto de la aplicación, que es más seguro para la BD
    val applicationForRepo = activityContext.applicationContext as TodoApplication

    val viewModel: NoteDetailViewModel = viewModel(
        factory = NoteViewModelFactory(applicationForRepo.repository)
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

    // 🚨 Usamos activityContext para los recursos de Media
    val recorder = remember { AudioRecorder(activityContext) }
    val audioPlayer = remember { AudioPlayer(activityContext) }
    var isRecording by remember { mutableStateOf(false) }

    // Asegura la liberación de recursos cuando la pantalla sale
    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.release()
            recorder.stop()
        }
    }


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
            // 🚨 Usamos activityContext para checkSelfPermission
            ContextCompat.checkSelfPermission(
                activityContext,
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
            // VISUALIZACIÓN DE MULTIMEDIA
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

                        // Muestra el control de reproducción si es AUDIO
                        if (multimedia.tipo == "AUDIO") {
                            AudioPlayerControl(
                                fileName = multimedia.uriArchivo,
                                player = audioPlayer
                            )
                        }

                        IconButton(
                            onClick = {
                                // Asegúrate de que el contexto para eliminar archivos use el context.filesDir que es el de la aplicación
                                viewModel.deleteMultimedia(multimedia, applicationForRepo.filesDir)
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