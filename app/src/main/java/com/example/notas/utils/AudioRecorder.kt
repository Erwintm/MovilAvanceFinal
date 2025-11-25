package com.example.notas.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.lang.RuntimeException

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null

    // Usamos 'lateinit' para referenciar el archivo de destino.
    private lateinit var outputFile: File

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

            // Simplificación: Usar la ruta absoluta para todas las versiones.
            // Esto es más estable que usar FileDescriptor y evita muchos crashes.
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