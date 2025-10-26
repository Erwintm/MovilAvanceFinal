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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.notas.data.Note
import com.example.notas.viewmodel.NoteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddNote(navController: NavController) {
    val context = LocalContext.current.applicationContext as TodoApplication
    val viewModel = remember { NoteViewModel(context.repository) }

    AddNoteScreen(
        onAddNote = { note ->
            viewModel.insert(note)
            navController.popBackStack()
        },
        onCancel = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(onAddNote: (Note) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var seleccionarTipo by remember { mutableStateOf("Notes") }
    var fechaLimite by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri?.toString()
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Agregar nota/tarea") }) },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = onCancel) { Text("Cancelar") }
                    Button(onClick = { launcher.launch("image/*") }) { Text("Agregar archivos") }
                    Button(
                        onClick = {
                            val tipo = if (seleccionarTipo == "Notes") 1 else 2
                            val note = Note(
                                title = title.ifBlank { "(sin título)" },
                                description = description,
                                imageUri = imageUri,
                                idTipo = tipo,
                                fechaLimite = if (tipo == 2) fechaLimite.ifBlank { null } else null,
                                hora = if (tipo == 2) hora.ifBlank { null } else null,
                                estado = if (tipo == 2) "Pendiente" else "Pendiente"
                            )
                            onAddNote(note)
                            title = ""
                            description = ""
                            imageUri = null
                            fechaLimite = ""
                            hora = ""
                            seleccionarTipo = "Notes"
                        },
                        enabled = title.isNotBlank() && description.isNotBlank()
                    ) { Text("Agregar") }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                    RadioButton(selected = seleccionarTipo == "Notes", onClick = { seleccionarTipo = "Notes" })
                    Text("Notes", modifier = Modifier.padding(start = 4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = seleccionarTipo == "Tasks", onClick = { seleccionarTipo = "Tasks" })
                    Text("Tasks", modifier = Modifier.padding(start = 4.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Descripción", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 300.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.medium)
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        coroutineScope.launch { delay(10); scrollState.scrollTo(scrollState.maxValue) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.Black),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default
                    ),
                    decorationBox = { innerTextField -> if (description.isEmpty()) Text("Escribe la descripción aquí...", color = Color.Gray); innerTextField() }
                )
            }

            if (seleccionarTipo == "Tasks") {
                OutlinedTextField(value = fechaLimite, onValueChange = { fechaLimite = it }, label = { Text("Fecha límite (ej. 2025-10-31)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = hora, onValueChange = { hora = it }, label = { Text("Hora (ej. 14:30)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }

            if (imageUri != null) {
                AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp).padding(vertical = 4.dp))
            }
        }
    }
}
