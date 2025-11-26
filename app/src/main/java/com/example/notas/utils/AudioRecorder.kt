package com.example.notas.utils

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri // 👈 Importación requerida
import android.os.Build
import androidx.core.content.FileProvider // 👈 Importación requerida
import java.io.File
import java.lang.RuntimeException
import java.text.SimpleDateFormat // 👈 Importación requerida
import java.util.Date // 👈 Importación requerida
import java.util.Locale // 👈 Importación requerida

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null

    // Usamos 'lateinit' para referenciar el archivo de destino.
    private lateinit var outputFile: File

    // 🚨 FUNCIÓN FALTANTE AÑADIDA
    /**
     * Crea un archivo temporal con un nombre único para almacenar el audio grabado
     * y devuelve su Uri compatible con FileProvider.
     * Esto permite usar ActivityResultContracts.CaptureVideo() para grabar audio.
     */
    fun createAudioFileUri(): Uri {
        // Genera un nombre de archivo único con timestamp
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val audioFileName = "AUD_${timeStamp}.mp4"

        // Define el archivo dentro del directorio de cache (temporal)
        // Usamos cacheDir porque es temporal para la cámara. Luego movemos/procesamos el archivo.
        val audioFile = File(context.cacheDir, audioFileName)

        // Usa FileProvider para obtener una URI segura
        // La autoridad debe coincidir con el AndroidManifest (com.example.notas.fileprovider)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            audioFile
        )
    }

    fun start(fileName: String) {
        // 1. Crear el archivo de salida
        outputFile = File(context.filesDir, fileName)

        // Limpiar cualquier instancia anterior fallida antes de empezar
        stopAndRelease()

        // 2. Inicializar el MediaRecorder de la forma adecuada para el SO
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            @Suppress("DEPRECATION")
            setOutputFile(outputFile.absolutePath)

            try {
                // El orden PREPARE -> START es CRÍTICO
                prepare()
                start()
            } catch (e: Exception) {
                // Si falla al iniciar o preparar, liberamos y limpiamos
                e.printStackTrace()
                stopAndRelease()
            }
        }
    }

    /**
     * Detiene y libera el MediaRecorder de forma segura, devolviendo el nombre del archivo.
     */
    fun stop(): String? {
        val fileName = if (::outputFile.isInitialized) outputFile.name else null
        var fileExists = false

        try {
            // Detener primero, luego liberar.
            recorder?.stop()
            fileExists = if (::outputFile.isInitialized) outputFile.exists() else false
        } catch (e: RuntimeException) {
            // Captura errores como "stop failed" (grabación demasiado corta)
            e.printStackTrace()
        } finally {
            // Siempre liberar y limpiar en el bloque finally.
            stopAndRelease()
        }

        // Devolvemos el nombre del archivo solo si existía y fue inicializado.
        return if (fileExists) fileName else null
    }

    /**
     * Función auxiliar para liberar recursos de forma forzada.
     */
    private fun stopAndRelease() {
        recorder?.release()
        recorder = null
    }

    fun isRecording() = recorder != null
}