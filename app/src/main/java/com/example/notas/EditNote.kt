package com.example.notas

import android.net.Uri
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
//Entrada de Parámetros: Al igual que NoteDetailScreen,
// recibe todos los datos de la nota a editar directamente de la URL de navegación (desde MainActivity).
fun EditNoteScreen(
    navController: NavController,
    noteId: Int,
    initialTitle: String,
    initialDescription: String,
    imageUri: String?,
    idTipo: Int = 1,
    fechaLimiteInit: String? = null,
    horaInit: String? = null,
    estadoInit: String? = null
) {
    val context = LocalContext.current.applicationContext as TodoApplication

    val viewModel: EditNoteViewModel = viewModel(
        factory = NoteViewModelFactory(context.repository)
    )

    LaunchedEffect(key1 = noteId) {
        // Reconstruye el objeto Note a partir de los parámetros de navegación
        val initialNote = Note(
            id = noteId,
            title = initialTitle,
            description = initialDescription,
            imageUri = imageUri,
            idTipo = idTipo,
            fechaLimite = fechaLimiteInit,
            hora = horaInit,
            estado = estadoInit
        )
        //CARGA DE ESTADO: Llama al ViewModel para precargar todos sus campos internos
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {



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

// Muestra los campos adicionales solo si el VM indica que es una Tarea.
            if (viewModel.seleccionarTipo == "Tasks") {
                OutlinedTextField(
                    value = viewModel.fechaLimite,
                    onValueChange = viewModel::updateFechaLimite,
                    label = { Text(stringResource(R.string.fecha_límite), color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )

                OutlinedTextField(
                    value = viewModel.hora,
                    onValueChange = viewModel::updateHora,
                    label = { Text(stringResource(R.string.hora), color = Color.White) },
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

            // BOTÓN GUARDAR
            Button(
                onClick = {

                    viewModel.updateNote()

// 2. Obtiene el objeto final actualizado (localmente)

                    val updatedNote = viewModel.buildUpdatedNote()

                    val titleEncoded = Uri.encode(updatedNote.title)
                    val descEncoded = Uri.encode(updatedNote.description)
                    val imgEncoded = updatedNote.imageUri?.let { Uri.encode(it) } ?: ""
                    val fechaEncoded = updatedNote.fechaLimite?.let { Uri.encode(it) } ?: ""
                    val horaEncoded = updatedNote.hora?.let { Uri.encode(it) } ?: ""
                    val estadoEncoded = updatedNote.estado?.let { Uri.encode(it) } ?: "Pendiente"

                    navController.popBackStack()
                    navController.navigate("noteDetail/${updatedNote.id}/$titleEncoded/$descEncoded/$imgEncoded/${updatedNote.idTipo}/$fechaEncoded/$horaEncoded/$estadoEncoded")
                },

                enabled = viewModel.isEntryValid,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.guardar_cambios)) }

            // BOTÓN CANCELAR
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cancelar))
            }
        }
    }
}