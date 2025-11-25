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
import android.net.Uri // 👈 ¡IMPORTANTE: Importación de Uri!

// NUEVAS IMPORTACIONES AÑADIDAS para Multimedia y Permisos
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.notas.utils.AudioRecorder
import com.example.notas.data.Multimedia
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
// ------------------------------

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

    // ⚠️ INICIALIZAR EL VIEWMODEL para cargar la Multimedia de esta nota
    LaunchedEffect(key1 = noteId) {
        viewModel.initialize(noteId)
    }

    // Reconstrucción del Objeto Note (Estado)
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
    // LÓGICA DE GRABACIÓN DE AUDIO Y PERMISOS (Unidad 8)
    // ----------------------------------------------------------------------

    val recorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }

    // Manejador para solicitar el permiso RECORD_AUDIO
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso OK: Iniciar grabación
            val fileName = "audio_${System.currentTimeMillis()}.mp4"
            recorder.start(fileName)
            isRecording = true
        } else {
            // Permiso Denegado
        }
    }

    // Función que inicia el flujo de grabación
    val startRecordingFlow: () -> Unit = {
        when {
            // 1. Verificar si ya tenemos el permiso
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Si ya lo tiene, iniciar grabación inmediatamente
                val fileName = "audio_${System.currentTimeMillis()}.mp4"
                recorder.start(fileName)
                isRecording = true
            }
            // 2. Solicitar el permiso (el launcher manejará la respuesta)
            else -> {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Lógica para detener la grabación
    val stopRecordingFlow: () -> Unit = {
        val fileName = recorder.stop()
        isRecording = false
        if (fileName != null) {
            // 3. Insertar el registro en la BD (Unidad 7)
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

            // ----------------------------------------------------------------------
            // Botón de Grabación (NUEVA FUNCIONALIDAD)
            // ----------------------------------------------------------------------
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
            // VISUALIZACIÓN DE MULTIMEDIA (NUEVA SECCIÓN)
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
                        // Botón de Reproducción (Pendiente de implementar en el siguiente paso)
                        Button(
                            onClick = { /* Lógica de Reproducción */ },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text("Play")
                        }

                        IconButton(
                            onClick = {
                                // Eliminar registro de BD y archivo físico
                                viewModel.deleteMultimedia(multimedia, context.filesDir)
                            }
                        ) {
                            // Ícono Corregido usando VectorDrawable estándar
                            Icon(
                                imageVector = ImageVector.vectorResource(id = android.R.drawable.ic_delete),
                                contentDescription = "Eliminar",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }

            // ----------------------------------------------------------------------
            // Contenido Antiguo (Imagen simple)
            // ----------------------------------------------------------------------
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

                Button(
                    onClick = {
                        // 🚀 CORRECCIÓN APLICADA: Codificar TODOS los argumentos de cadena antes de navegar.
                        val titleEncoded = Uri.encode(currentNote.title)
                        val descEncoded = Uri.encode(currentNote.description)

                        // imageUri puede ser null, lo manejamos y codificamos el resultado.
                        val imgEncoded = currentNote.imageUri?.let { Uri.encode(it) } ?: Uri.encode("")

                        // Campos de Tarea: Si son nulos, usamos "", y luego codificamos.
                        val fechaEncoded = Uri.encode(currentNote.fechaLimite ?: "")
                        val horaEncoded = Uri.encode(currentNote.hora ?: "")
                        val estadoEncoded = Uri.encode(currentNote.estado ?: "")

                        // Navega a la ruta de edición con los argumentos codificados
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

                // Botón Regresar
                Button(onClick = { navController.popBackStack("main", inclusive = false) }) {
                    Text(stringResource(R.string.regresar))
                }
            }
        }
    }
}