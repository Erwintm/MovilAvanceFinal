package com.example.notas.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilitario para manejar la creación de archivos temporales para la grabación de video.
 */
class VideoRecorder(private val context: Context) {

    // 🟢 NUEVA VARIABLE: Almacenará la referencia al último archivo temporal creado.
    private lateinit var videoFile: File

    /**
     * Crea un archivo temporal con un nombre único para almacenar el video grabado
     * y devuelve su Uri compatible con FileProvider.
     * @return La Uri temporal donde la aplicación de la cámara guardará el video.
     */
    fun createVideoFileUri(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val videoFileName = "VID_${timeStamp}.mp4"

        // Define el archivo dentro del directorio de cache
        // 🟢 ASIGNACIÓN: Guardamos la referencia para usarla después de la grabación.
        videoFile = File(context.cacheDir, videoFileName)

        // Usa FileProvider para obtener una URI segura
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            videoFile
        )
    }

    /**
     * 🟢 NUEVA FUNCIÓN: Devuelve el objeto File del video temporal creado.
     * Es crucial para mover el archivo de caché a almacenamiento permanente.
     */
    fun getTempVideoFile(): File? {
        return if (::videoFile.isInitialized) videoFile else null
    }
}