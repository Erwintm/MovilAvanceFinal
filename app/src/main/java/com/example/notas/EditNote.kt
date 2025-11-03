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
import com.example.notas.viewmodel.EditNoteViewModel // 👈 Importamos el nuevo ViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel // 👈 Importación clave
import androidx.compose.ui.text.TextStyle // Necesario para LocalTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
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

    // 1. INYECTAR EL VIEWMODEL ESPECÍFICO DE LA PANTALLA
    val viewModel: EditNoteViewModel = viewModel(
        factory = NoteViewModelFactory(context.repository)
    )

    // 2. EFECTO LATERAL: Inicializar el ViewModel con los datos de navegación
    // Esto asegura que solo se haga una vez al inicio del ciclo de vida del ViewModel.
    LaunchedEffect(key1 = noteId) {
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
            // ⚠️ Se eliminaron todas las variables 'var xxx by remember { mutableStateOf(...) }'
            // Ahora leemos directamente del ViewModel.

            // CAMPO DE TÍTULO
            OutlinedTextField(
                value = viewModel.title, // 👈 Leemos del ViewModel
                onValueChange = viewModel::updateTitle, // 👈 Escribimos al ViewModel
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

            // CAMPOS DE TAREA (CONDICIONALES)
            if (viewModel.seleccionarTipo == "Tasks") { // 👈 Condición basada en el estado del ViewModel
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
                    // 3. EL VIEWMODEL HACE LA LÓGICA DE ACTUALIZACIÓN
                    viewModel.updateNote()

                    // 4. Lógica de navegación (esto es compleja, la simplificamos)
                    // PUSH: Si la navegación después de la edición es compleja, a veces es mejor
                    // simplemente volver y dejar que la pantalla de detalles se actualice sola
                    // con el Flow, pero seguiremos tu lógica original:

                    val updatedNote = viewModel.buildUpdatedNote() // Función auxiliar (ver nota al final)

                    val titleEncoded = Uri.encode(updatedNote.title)
                    val descEncoded = Uri.encode(updatedNote.description)
                    val imgEncoded = updatedNote.imageUri?.let { Uri.encode(it) } ?: ""
                    val fechaEncoded = updatedNote.fechaLimite?.let { Uri.encode(it) } ?: ""
                    val horaEncoded = updatedNote.hora?.let { Uri.encode(it) } ?: ""
                    val estadoEncoded = updatedNote.estado?.let { Uri.encode(it) } ?: "Pendiente"

                    navController.popBackStack()
                    navController.navigate("noteDetail/${updatedNote.id}/$titleEncoded/$descEncoded/$imgEncoded/${updatedNote.idTipo}/$fechaEncoded/$horaEncoded/$estadoEncoded")
                },
                // 5. USAMOS LA VALIDACIÓN DEL VIEWMODEL
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