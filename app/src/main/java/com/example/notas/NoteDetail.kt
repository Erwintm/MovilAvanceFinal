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
import androidx.compose.foundation.border
import android.app.Activity // IMPORTACIÓN AÑADIDA
import android.content.Intent // IMPORTACIÓN AÑADIDA
import android.provider.Settings // IMPORTACIÓN AÑADIDA


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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavController,
    noteId: Int,
    title: String,
    description: String,

    idTipo: Int = 1,
    fechaLimite: String? = null,
    hora: String? = null,
    estado: String? = null
) {
    val activityContext = LocalContext.current
    // NECESARIO: Convertir el contexto a Activity para el manejo avanzado de permisos
    val activity = activityContext as Activity
    val applicationContextForRepo = activityContext.applicationContext as TodoApplication

    val viewModel: NoteDetailViewModel = viewModel(
        factory = NoteViewModelFactory(applicationContextForRepo.repository)
    )

    LaunchedEffect(key1 = noteId) {
        viewModel.initialize(noteId)
    }

    val audioPlayer = remember { AudioPlayer(activityContext) }


    val currentNote: Note by viewModel.note.collectAsState()
    val multimediaList by viewModel.multimediaList.collectAsState()



    val audioRecorder = remember { AudioRecorder(activityContext) }
    val videoRecorder = remember { VideoRecorder(activityContext) }

    var isRecordingAudio by remember { mutableStateOf(false) }
    var tempVideoUri by remember { mutableStateOf<Uri?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.release()
            audioRecorder.stop()
        }
    }


    // --- 1. DECLARACIÓN DE LOS LAUNCHERS (DEBE ESTAR ANTES DEL SCAFFOLD) ---

    // Este launcher (CaptureVideo) no tiene dependencias cruzadas con los flujos de permiso
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        // si el video fue grabado
        if (success) {
            // recuperea el arhcivo de la cahe
            val tempFile = videoRecorder.getTempVideoFile()

            if (tempFile != null && tempFile.exists()) {
                // construye una ruta para guardarse temporalmete
                val permanentFileName = tempFile.name

                val destinationFile = File(applicationContextForRepo.filesDir, permanentFileName)

                try {
                    // mueve el arhcivo
                    tempFile.copyTo(destinationFile, overwrite = true)
                    tempFile.delete()


                    val newMultimedia = Multimedia(
                        notaId = currentNote.id,
                        uriArchivo = permanentFileName,
                        tipo = "VIDEO"
                    )
                    viewModel.insertMultimedia(newMultimedia)

                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }
        }
        tempVideoUri = null
    }

    // AHORA DECLARAMOS LOS LAUNCHERS DE PERMISOS, que usaremos en las funciones de flujo (paso 3)
    lateinit var requestAudioPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>
    lateinit var requestCameraPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>


    // --- 2. DEFINICIÓN DE FLUJOS (Funciones Lambda) ---
    // Estas lambdas DEBEN estar definidas antes de los rememberLauncherForActivityResult que las invocan.

    val startStopRecordingAudioFlow: () -> Unit = {
        when {
            isRecordingAudio -> {
                // 1. Detener Grabación
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

            ContextCompat.checkSelfPermission(
                activityContext, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 2. Permiso Concedido: Iniciar Grabación
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val audioFileName = "AUD_${timeStamp}.mp4"

                audioRecorder.start(audioFileName)
                isRecordingAudio = true
            }
            else -> {
                // 3. Manejo de Permiso Denegado/No Solicitado
                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    // Denegación temporal: Solicitar permiso estándar
                    requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    // Denegación permanente: Navegar a la Configuración de la aplicación
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", activity.packageName, null)
                    )
                    activity.startActivity(intent)
                }
            }
        }
    }


    val startRecordingVideoFlow: () -> Unit = {
        when {
            // verifica los permisos
            ContextCompat.checkSelfPermission(
                activityContext, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Se crea un archivo temporarl en la memoria del cache
                tempVideoUri = videoRecorder.createVideoFileUri()

                tempVideoUri?.let { uri ->
                    // intent le dice al sistema que abra la camara
                    videoLauncher.launch(uri)
                }
            }
            else -> {
                // Manejo de Permiso Denegado/No Solicitado
                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    // Denegación temporal: Solicitar permiso estándar
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                } else {
                    // Denegación permanente: Navegar a la Configuración de la aplicación
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", activity.packageName, null)
                    )
                    activity.startActivity(intent)
                }
            }
        }
    }


    // --- 3. DEFINICIÓN DE LAUNCHERS DE PERMISOS (USANDO LAS FUNCIONES DE FLUJO) ---

    requestAudioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Si el permiso es concedido, re-ejecutamos el flujo para iniciar la grabación.
            startStopRecordingAudioFlow()
        }
    }

    requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Si el permiso es concedido, re-ejecutamos el flujo para lanzar la cámara.
            startRecordingVideoFlow()
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (currentNote.idTipo == 1) "Detalle de Nota" else "Detalle de Tarea",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor =  Color(0xFF121212)),
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
                    navController.navigate("recordatorios/$noteId")
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.ver_recordatorios))
            }


            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Botón para iniciar/detener la grabación
                Button(
                    onClick = startStopRecordingAudioFlow, // <<-- FUNCIÓN UTILIZADA
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecordingAudio) Color.Red else Color(0xFF455A64)
                    )
                ) {
                    Text(if (isRecordingAudio) "Detener Grabación" else "Grabar Audio")
                }

                // Botón para grabar video
                Button(
                    onClick = startRecordingVideoFlow, // <<-- FUNCIÓN UTILIZADA
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text("Grabar Video")
                }
            }




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

                            // Muestra el botón de eliminar para todos los tipos
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

                        // === NUEVA LÓGICA: Muestra la IMAGEN Multimedia ===
                        if (multimedia.tipo == "IMAGEN") {
                            // Construye el objeto File a partir del nombre guardado.
                            val imageFile = File(applicationContextForRepo.filesDir, multimedia.uriArchivo)

                            AsyncImage(
                                model = imageFile, // Usamos el objeto File local
                                contentDescription = "Imagen Adjunta",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(vertical = 4.dp)
                                    .border(1.dp, Color.Gray, MaterialTheme.shapes.small)
                            )
                        }
                        // ===================================================
                    }
                }
            }




            if (!currentNote.imageUri.isNullOrBlank()) {

                val imageFile = File(applicationContextForRepo.filesDir, currentNote.imageUri!!)




                AsyncImage(
                    model = imageFile, // Usamos directamente el objeto File, que Coil puede resolver
                    contentDescription = "Imagen Adjunta (Legacy)",
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
                        val fechaEncoded = Uri.encode(currentNote.fechaLimite ?: "")
                        val horaEncoded = Uri.encode(currentNote.hora ?: "")
                        val estadoEncoded = Uri.encode(currentNote.estado ?: "")

                        navController.navigate(
                            // La ruta espera 7 argumentos (ID, Title, Desc, IDTipo, Fecha, Hora, Estado)
                            "editNote/${currentNote.id}/$titleEncoded/$descEncoded/${currentNote.idTipo}/$fechaEncoded/$horaEncoded/$estadoEncoded"
                        )
                    }
                ) { Text(stringResource(R.string.editar)) }



                Button(
                    onClick = {
                        viewModel.deleteNote(currentNote)
                        navController.popBackStack("main", inclusive = false)
                    },
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text(stringResource(R.string.eliminar), color = MaterialTheme.colorScheme.onError)
                }


                Button(onClick = { navController.popBackStack("main", inclusive = false) }) {
                    Text(stringResource(R.string.regresar))
                }


            }
            Spacer(Modifier.height(20.dp))
        }
    }
}