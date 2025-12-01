package com.example.notas.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilitario para manejar la creación de archivos temporales para la grabación de video.
 * Esta clase NO graba el video; solo prepara la ubicación y la URI para que la app de la cámara lo haga.
 */
class VideoRecorder(private val context: Context) {

    // Referencia al objeto File temporal donde la cámara del sistema guardará el video.
    // 'lateinit' indica que se inicializará más tarde (en createVideoFileUri).
    private lateinit var videoFile: File


    /**
     * Crea un nombre de archivo único, un archivo vacío en caché y devuelve una URI segura.
     * Esta URI es la que se pasa a la app de la cámara.
     */
    fun createVideoFileUri(): Uri {
        // Genera un sello de tiempo único (YYYYMMDD_HHmmss) para nombrar el archivo de forma única.
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        // Crea el nombre del archivo de video (ej: VID_20251127_093400.mp4).
        val videoFileName = "VID_${timeStamp}.mp4"

        // Crea el objeto File. IMPORTANTE: Usamos 'context.cacheDir' (Directorio Temporal)
        // porque la app de la cámara del sistema necesita escribir el resultado aquí.
        videoFile = File(context.cacheDir, videoFileName)

        // Usa FileProvider para obtener una URI segura
        // El FileProvider es necesario desde Android 7.0+ para compartir archivos con otras apps.
        return FileProvider.getUriForFile(
            context,
            // Esta 'authority' debe coincidir con la declarada en AndroidManifest.xml.
            "${context.packageName}.fileprovider",
            videoFile
        )
    }


    /**
     * Devuelve la referencia al archivo temporal que se creó.
     * Esto es usado en NoteDetail.kt para COPIAR el video del caché al almacenamiento permanente.
     * @return El objeto File temporal o null si aún no se ha creado la URI.
     */
    fun getTempVideoFile(): File? {
        // Verifica si la variable 'videoFile' ya fue inicializada.
        return if (::videoFile.isInitialized) videoFile else null
    }
}