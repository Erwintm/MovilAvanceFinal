package com.example.notas

import android.net.Uri
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
import com.example.notas.viewmodel.AddNoteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


import androidx.core.content.FileProvider
import java.io.File
import android.content.Context
import androidx.compose.foundation.background
import java.io.InputStream
import java.io.FileOutputStream
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private fun Uri.copyToInternalStorage(context: Context): String? {

    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val randomPart = UUID.randomUUID().toString().substring(0, 4)
    val fileName = "IMG_${timeStamp}_${randomPart}.jpg"


    val destinationFile = File(context.filesDir, fileName)

    try {
        // 3. Abrir InputStream desde la URI (funciona para content:// URIs y file:// URIs)
        context.contentResolver.openInputStream(this)?.use { inputStream ->

            destinationFile.outputStream().use { outputStream ->

                inputStream.copyTo(outputStream)
            }
        }

        return fileName
    } catch (e: Exception) {

        println("ERROR al copiar la URI al almacenamiento interno: ${e.message}")
        e.printStackTrace()
        return null
    }
}



private fun createImageFileUri(context: Context): Uri {
    // Crea el archivo DENTRO del directorio de caché (Temporal)
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    // Usa FileProvider para crear una URI que la cámara pueda usar
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}
// ------------------------------------------------------------------


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
    val context = LocalContext.current
    val applicationContext = context.applicationContext // Contexto para operaciones de archivo
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val internalFileNames = remember { mutableStateListOf<String>() }

    var cameraTempUri by remember { mutableStateOf<Uri?>(null) } // URI temporal de la cámara (cacheDir)



    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { externalUri ->

            val internalFileName = externalUri.copyToInternalStorage(applicationContext)


            internalFileName?.let { internalFileNames.add(it) }
        }
    }


    // Launcher para Cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        cameraTempUri?.let { tempUri ->
            if (success) {

                val internalFileName = tempUri.copyToInternalStorage(applicationContext)


                internalFileNames.add(internalFileName!!)


                try {
                    // Nota: Usamos context.cacheDir porque la URI temporal se creó allí
                    val tempFile = File(context.cacheDir, tempUri.lastPathSegment)
                    if (tempFile.exists()) tempFile.delete()
                } catch (e: Exception) {
                    println("Error al intentar borrar archivo temporal de cámara: ${e.message}")
                }
            }
            // Limpia la URI temporal de la cámara
            cameraTempUri = null
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.agregar), color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF121212))
            )
        },

        bottomBar = {
            // Fila de botones con peso uniforme
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121212))
                    // *** MODIFICADO: Padding horizontal a 0.dp para máxima anchura. ***
                    .padding(horizontal = 0.dp, vertical = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2)),

                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancelar), maxLines = 1)
                }


                Button(
                    onClick = { galleryLauncher.launch("image/*") },

                    modifier = Modifier.weight(1f)
                ) {
                    Text("Galería", maxLines = 1)
                }

                // 3. Botón Cámara (Mismo tamaño: weight=1f)
                Button(
                    onClick = {
                        val newUri = createImageFileUri(context)
                        cameraTempUri = newUri
                        cameraLauncher.launch(newUri)
                    },

                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cámara", maxLines = 1)
                }


                Button(
                    onClick = {
                        // Pasa la lista de NOMBRES de archivo internos (Strings) al ViewModel
                        viewModel.saveNoteWithMultimedia(internalFileNames.toList())
                        onSaveComplete()
                    },
                    enabled = viewModel.isEntryValid,

                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.agregar), maxLines = 1)
                }
            }
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        Column(

            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp) // Mantenemos el padding horizontal para el contenido
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
                        onClick = { viewModel.updateTipo("Notes") }
                    )
                    Text(stringResource(R.string.notas), modifier = Modifier.padding(start = 4.dp), color = Color.White)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = viewModel.seleccionarTipo == "Tasks",
                        onClick = { viewModel.updateTipo("Tasks") }
                    )
                    Text(stringResource(R.string.tareas), modifier = Modifier.padding(start = 4.dp), color = Color.White)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.descripcion), style = MaterialTheme.typography.labelLarge, color = Color.White)
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
                        if (viewModel.description.isEmpty()) Text(stringResource(R.string.escriDes), color = Color.Gray)
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

            //mosr
            if (internalFileNames.isNotEmpty()) {
                Text("Archivos Adjuntos:", style = MaterialTheme.typography.labelLarge, color = Color.White, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(4.dp))

                internalFileNames.forEach { internalFileName ->
                    // Construye el objeto File a partir del nombre guardado.
                    val imageFile = File(applicationContext.filesDir, internalFileName)

                    AsyncImage(
                        model = imageFile, // Coil puede cargar directamente desde un objeto File
                        contentDescription = "Imagen adjunta",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(vertical = 4.dp)
                            .border(1.dp, Color.Gray, MaterialTheme.shapes.small)
                    )
                }
            }
        }
    }
}