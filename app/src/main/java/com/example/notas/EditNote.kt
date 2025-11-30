package com.example.notas

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.notas.data.Note
import com.example.notas.viewmodel.EditNoteViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.TextStyle
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import android.content.Context
import androidx.compose.foundation.background


// FUNCIÓN UTILITARIA PARA CREAR URI TEMPORAL DE LA CÁMARA

private fun createImageFileUri(context: Context): Uri {
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    // Usa FileProvider para crear una URI que la cámara pueda usar
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}
// ------------------------------------------------------------------


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    navController: NavController,
    noteId: Int,
    initialTitle: String,
    initialDescription: String,
    // ELIMINADO: imageUri: String?
    idTipo: Int = 1,
    fechaLimiteInit: String? = null,
    horaInit: String? = null,
    estadoInit: String? = null
) {
    val context = LocalContext.current.applicationContext as TodoApplication

    val viewModel: EditNoteViewModel = viewModel(
        factory = NoteViewModelFactory(context.repository)
    )

    val scrollState = rememberScrollState()

    // Lista para almacenar nuevas URIs adjuntadas durante esta sesión de edición
    val tempNewImageUris = remember { mutableStateListOf<Uri>() }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }


    // Lanzadores de Multimedia
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { tempNewImageUris.add(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraUri != null) {
            tempNewImageUris.add(cameraUri!!)
        }
        cameraUri = null
    }

    LaunchedEffect(key1 = noteId) {
        // Reconstruye el objeto Note a partir de los parámetros de navegación
        val initialNote = Note(
            id = noteId,
            title = initialTitle,
            description = initialDescription,
            // imageUri eliminado en la inicialización
            idTipo = idTipo,
            fechaLimite = fechaLimiteInit,
            hora = horaInit,
            estado = estadoInit
        )

        viewModel.initializeState(initialNote)
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.editarNT), color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))

            // CAMPO DE TÍTULO
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = viewModel::updateTitle,
                label = { Text(stringResource(R.string.titulo), color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = Color.White)
            )

            // CAMPO DE DESCRIPCIÓN
            OutlinedTextField(
                value = viewModel.description,
                onValueChange = viewModel::updateDescription,
                label = { Text(stringResource(R.string.descripcion), color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = Color.White)
            )

            // CAMPOS DE TAREA (si aplica)
            if (viewModel.seleccionarTipo == "Tasks") {
                OutlinedTextField(
                    value = viewModel.fechaLimite,
                    onValueChange = viewModel::updateFechaLimite,
                    label = { Text(stringResource(R.string.fechaLit), color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )

                OutlinedTextField(
                    value = viewModel.hora,
                    onValueChange = viewModel::updateHora,
                    label = { Text(stringResource(R.string.esHora), color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )

                OutlinedTextField(
                    value = viewModel.estado,
                    onValueChange = viewModel::updateEstado,
                    label = { Text(stringResource(R.string.estado), color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )
            }

            // VISUALIZACIÓN DE ARCHIVOS MULTIMEDIA EXISTENTES
            if (viewModel.multimediaUris.isNotEmpty()) {
                Text("Archivos Existentes:", style = MaterialTheme.typography.labelLarge, color = Color.White)
                viewModel.multimediaUris.forEach { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Archivo multimedia existente",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(vertical = 4.dp)
                            .border(1.dp, Color.Gray, MaterialTheme.shapes.small)
                    )
                }
            }

            // VISUALIZACIÓN DE NUEVOS ARCHIVOS MULTIMEDIA ADJUNTADOS EN ESTA SESIÓN
            if (tempNewImageUris.isNotEmpty()) {
                Text("Nuevos Archivos a Añadir:", style = MaterialTheme.typography.labelLarge, color = Color.White)
                tempNewImageUris.forEach { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Nuevo archivo adjunto",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(vertical = 4.dp)
                            .border(1.dp, Color.Green, MaterialTheme.shapes.small) // Borde verde para diferenciar
                    )
                }
            }

            // BOTONES DE MULTIMEDIA
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) { Text("Galería", maxLines = 1) }

                Button(
                    onClick = {
                        val newUri = createImageFileUri(context)
                        cameraUri = newUri
                        cameraLauncher.launch(newUri)
                    },
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) { Text("Cámara", maxLines = 1) }
            }


            // BOTÓN GUARDAR CAMBIOS
            Button(
                onClick = {
                    // *** LLAMADA CORREGIDA ***: Pasa solo los archivos multimedia nuevos.
                    viewModel.updateNoteWithMultimedia(tempNewImageUris.map { it.toString() })

                    // La navegación es más sencilla si solo volvemos atrás después de guardar.
                    // Si necesitamos refrescar NoteDetail, el onSnapshot debería hacerlo automáticamente.
                    navController.popBackStack()
                },
                enabled = viewModel.isEntryValid,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.guardar_cambios)) }

            // BOTÓN CANCELAR
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cancelar))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}