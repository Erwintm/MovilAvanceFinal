package com.example.notas

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
// Se eliminan las importaciones relacionadas con Uri, cámara, galería, FileProvider y AsyncImage.


// FUNCIÓN UTILITARIA PARA CREAR URI TEMPORAL DE LA CÁMARA
// Esta función fue eliminada para limpiar el código de multimedia.
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
    // Se eliminó la declaración de tempNewImageUris.
    // Se eliminó la declaración de cameraUri.


    // Lanzadores de Multimedia
    // Se eliminó la declaración de galleryLauncher.
    // Se eliminó la declaración de cameraLauncher.

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
            // Este bloque de código ha sido eliminado.

            // VISUALIZACIÓN DE NUEVOS ARCHIVOS MULTIMEDIA ADJUNTADOS EN ESTA SESIÓN
            // Este bloque de código ha sido eliminado.

            // BOTONES DE MULTIMEDIA
            // Este Row de botones de multimedia ha sido eliminado.


            // BOTÓN GUARDAR CAMBIOS
            Button(
                onClick = {
                    // *** CORRECCIÓN ***: Usar la función existente y pasar una lista vacía.
                    viewModel.updateNoteWithMultimedia(emptyList())

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