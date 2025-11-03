package com.example.notas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.notas.data.Note

import com.example.notas.viewmodel.AddNoteViewModel
import com.example.notas.NoteViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddNote(navController: NavController) {
    val context = LocalContext.current.applicationContext as TodoApplication


    val viewModel: AddNoteViewModel = viewModel(
        factory = NoteViewModelFactory(context.repository)
    )

    AddNoteScreen(
        viewModel = viewModel,
        onSaveComplete = { navController.popBackStack() },
        onCancel = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    viewModel: AddNoteViewModel,
    onSaveComplete: () -> Unit,
    onCancel: () -> Unit
) {


    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // La UI llama al handler del ViewModel para actualizar el estado
        viewModel.updateImageUri(uri?.toString())
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.agregar), color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color(0xFF121212)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                    ) { Text(stringResource(R.string.cancelar)) }

                    Button(onClick = { launcher.launch("image/*") }) { Text(stringResource(R.string.agregar_archivos)) }

                    Button(
                        onClick = {

                            viewModel.saveNote()
                            onSaveComplete()
                        },

                        enabled = viewModel.isEntryValid
                    ) { Text(stringResource(R.string.agregar)) }
                }
            }
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // CAMPO DE TÍTULO
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = viewModel::updateTitle,
                label = { Text(stringResource(R.string.titulo), color = Color.White) },
                textStyle = TextStyle(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // RADIO BUTTONS
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                    RadioButton(
                        selected = viewModel.seleccionarTipo == "Notes",
                        onClick = { viewModel.updateTipo("Notes") } // 👈 Evento al ViewModel
                    )
                    Text(stringResource(R.string.notas), modifier = Modifier.padding(start = 4.dp), color = Color.White)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = viewModel.seleccionarTipo == "Tasks",
                        onClick = { viewModel.updateTipo("Tasks") } // 👈 Evento al ViewModel
                    )
                    Text(stringResource(R.string.tareas), modifier = Modifier.padding(start = 4.dp), color = Color.White)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.descripcion), style = MaterialTheme.typography.labelLarge, color = Color.White) // Uso del nuevo string
            Spacer(Modifier.height(4.dp))

            // CAMPO DE DESCRIPCIÓN
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 300.dp)
                    .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.medium)
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = viewModel.description,
                    onValueChange = {
                        viewModel.updateDescription(it)
                        coroutineScope.launch { delay(10); scrollState.scrollTo(scrollState.maxValue) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default
                    ),
                    decorationBox = { innerTextField ->
                        if (viewModel.description.isEmpty()) Text(stringResource(R.string.escriDes), color = Color.Gray) // Uso del nuevo string
                        innerTextField()
                    }
                )
            }


            if (viewModel.seleccionarTipo == "Tasks") {
                OutlinedTextField(
                    value = viewModel.fechaLimite,
                    onValueChange = viewModel::updateFechaLimite,
                    label = { Text(stringResource(R.string.fechaLit), color = Color.White) },
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.hora,
                    onValueChange = viewModel::updateHora,
                    label = { Text(stringResource(R.string.esHora), color = Color.White) },
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
            }

            // IMAGEN
            if (viewModel.imageUri != null) {
                AsyncImage(
                    model = viewModel.imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(180.dp).padding(vertical = 4.dp)
                )
            }
        }
    }
}