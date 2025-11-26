package com.example.notas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.core.content.ContextCompat
import com.example.notas.utils.AudioRecorder
import com.example.notas.data.Multimedia
import com.example.notas.utils.AudioPlayer
import com.example.notas.utils.VideoRecorder
import androidx.compose.ui.viewinterop.AndroidView

// 🚨 Importaciones necesarias para la generación de nombre de archivo y reproductor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File
import androidx.compose.runtime.DisposableEffect


// ----------------------------------------------------------------------
// COMPONENTES DE REPRODUCCIÓN
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

@Composable
fun VideoPlayer(uri: Uri) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.Builder().setUri(uri).setMimeType(MimeTypes.VIDEO_MP4).build())
            prepare()
            playWhenReady = false
        }
    }

    DisposableEffect(key1 = uri) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(vertical = 8.dp)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}


// ----------------------------------------------------------------------
// PANTALLA PRINCIPAL: NoteDetailScreen
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
    val activityContext = LocalContext.current
    val applicationContextForRepo = activityContext.applicationContext as TodoApplication

    val viewModel: NoteDetailViewModel = viewModel(
        factory = NoteViewModelFactory(applicationContextForRepo.repository)
    )

    LaunchedEffect(key1 = noteId) {
        viewModel.initialize(noteId)
    }

    val audioPlayer = remember { AudioPlayer(activityContext) }

    val currentNote by viewModel.note.collectAsState()
    val multimediaList by viewModel.multimediaList.collectAsState()

    // ----------------------------------------------------------------------
    // LÓGICA DE GRABACIÓN DE AUDIO Y VIDEO
    // ----------------------------------------------------------------------

    val audioRecorder = remember { AudioRecorder(activityContext) }
    val videoRecorder = remember { VideoRecorder(activityContext) }

    var isRecordingAudio by remember { mutableStateOf(false) }
    // ❌ ELIMINADO: tempAudioUri ya no se usa para el lanzador de la cámara
    // var tempAudioUri by remember { mutableStateOf<Uri?>(null) }
    var tempVideoUri by remember { mutableStateOf<Uri?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.release()
            // Llama a stop por si acaso la grabación se detuvo sin el botón
            audioRecorder.stop()
        }
    }

    // ❌ ELIMINADO: audioLauncher ya no se usa para el audio
    /* val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) {
            tempAudioUri?.let { uri ->
                val fileName = uri.pathSegments.last()
                val newMultimedia = Multimedia(
                    notaId = currentNote.id,
                    uriArchivo = fileName,
                    tipo = "AUDIO"
                )
                viewModel.insertMultimedia(newMultimedia)
            }
        }
        isRecordingAudio = false
        tempAudioUri = null
    }
    */

    // --- LANZADOR DE VIDEO
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) {
            tempVideoUri?.let { uri ->
                val fileName = uri.pathSegments.last()
                val newMultimedia = Multimedia(
                    notaId = currentNote.id,
                    uriArchivo = fileName,
                    tipo = "VIDEO"
                )
                viewModel.insertMultimedia(newMultimedia)
            }
        }
        tempVideoUri = null
    }

    // 🟢 NUEVO FLUJO DE AUDIO (TOGGLE INTERNO)
    val startStopRecordingAudioFlow: () -> Unit = {
        when {
            isRecordingAudio -> {
                // 🛑 Lógica de DETENER (Stop)
                val fileName = audioRecorder.stop()
                if (!fileName.isNullOrBlank()) {
                    val newMultimedia = Multimedia(
                        notaId = currentNote.id,
                        uriArchivo = fileName,
                        tipo = "AUDIO"
                    )
                    viewModel.insertMultimedia(newMultimedia)
                }
                isRecordingAudio = false
            }
            // 🟢 Lógica de INICIAR (Start)
            ContextCompat.checkSelfPermission(
                activityContext, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val audioFileName = "AUD_${timeStamp}.mp4"

                audioRecorder.start(audioFileName)
                isRecordingAudio = true
            }
            else -> { /* Solicitar permiso */ }
        }
    }

    // --- FLUJO DE INICIO DE GRABACIÓN DE VIDEO (CORREGIDO CON ?.let)
    val startRecordingVideoFlow: () -> Unit = {
        when {
            ContextCompat.checkSelfPermission(
                activityContext, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                tempVideoUri = videoRecorder.createVideoFileUri()
                // 🚨 Corrección de URI: lanzar solo si no es nula
                tempVideoUri?.let { uri ->
                    videoLauncher.launch(uri)
                }
            }
            else -> { /* Solicitar permiso */ }
        }
    }


    // ----------------------------------------------------------------------
    // SCAFFOLD
    // ----------------------------------------------------------------------

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (currentNote.idTipo == 1) "Detalle de Nota" else "Detalle de Tarea",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212)),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver),
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("${stringResource(R.string.titulo)}: ${currentNote.title}", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text("Descripción: ${currentNote.description}", color = Color.White)

            if (currentNote.idTipo == 2) {
                Text("${stringResource(R.string.fecha_límite)}: ${currentNote.fechaLimite ?: "-"}", color = Color.White)
                Text("${stringResource(R.string.hora)}: ${currentNote.hora ?: "-"}", color = Color.White)
                Text("${stringResource(R.string.estado)}: ${currentNote.estado ?: "Pendiente"}", color = Color.White)
            }

            // Botón para Recordatorio
            Button(
                onClick = {
                    navController.navigate("addRecordatorio/${currentNote.id}")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
            ) {
                Text("Agregar recordatorio")
            }

            // ----------------------------------------------------------------------
            // BOTONES DE GRABACIÓN (AUDIO Y VIDEO)
            // ----------------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = startStopRecordingAudioFlow, // ⬅️ Usando el flujo de toggle corregido
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecordingAudio) Color.Red else Color.Green
                    )
                ) {
                    Text(if (isRecordingAudio) "Detener Grabación" else "Grabar Audio")
                }

                Button(
                    onClick = startRecordingVideoFlow,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                ) {
                    Text("Grabar Video")
                }
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
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${multimedia.tipo}: ${multimedia.uriArchivo.takeLast(20)}",
                                color = Color.LightGray,
                                modifier = Modifier.weight(1f)
                            )

                            if (multimedia.tipo == "AUDIO") {
                                AudioPlayerControl(
                                    fileName = multimedia.uriArchivo,
                                    player = audioPlayer
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.deleteMultimedia(multimedia, applicationContextForRepo.filesDir)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = Color.Red
                                )
                            }
                        }

                        // Muestra el reproductor de video
                        if (multimedia.tipo == "VIDEO") {
                            // Construye la URI del archivo desde el disco
                            val videoFile = File(applicationContextForRepo.filesDir, multimedia.uriArchivo)
                            val videoUri = Uri.fromFile(videoFile)
                            VideoPlayer(uri = videoUri)
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

            // Botones de acción principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Editar
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


                // Botón Eliminar
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
            Spacer(Modifier.height(20.dp))
        }
    }
}