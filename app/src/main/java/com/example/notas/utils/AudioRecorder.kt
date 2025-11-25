package com.example.notas.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.lang.RuntimeException

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null

    // Usamos 'lateinit' para referenciar el archivo de destino.
    private lateinit var outputFile: File

    fun start(fileName: String) {
        // 1. Crear el archivo de salida en el directorio interno de la app
        outputFile = File(context.filesDir, fileName)

        // Inicializar el MediaRecorder
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

            // Simplificación y robustez: Usar la ruta absoluta para todas las versiones.
            // Esto evita problemas con FileDescriptor en diferentes versiones de SO.
            @Suppress("DEPRECATION")
            setOutputFile(outputFile.absolutePath)

            try {
                prepare()
                start()
            } catch (e: Exception) {
                // Si falla al iniciar o preparar (ej: permiso denegado, recurso en uso)
                e.printStackTrace()
                release() // Liberamos recursos
                recorder = null
            }
        }
    }

    /**
     * Detiene y libera el MediaRecorder de forma segura.
     * Devuelve el nombre del archivo si la grabación fue al menos parcial.
     */
    fun stop(): String? {
        // 1. Guardamos las referencias antes de anularlas, asegurando que se inicializó
        val fileName = if (::outputFile.isInitialized) outputFile.name else null
        val fileExists = if (::outputFile.isInitialized) outputFile.exists() else false

        try {
            // Intenta detener y liberar los recursos
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: RuntimeException) {
            // 2. Captura errores comunes como "stop failed" (cuando la grabación es muy corta)
            e.printStackTrace()
            // Asegúrate de liberar los recursos aunque la parada haya fallado
            recorder?.release()
        } finally {
            // 3. Siempre aseguramos que la referencia se limpie
            recorder = null
        }

        // Devolvemos el nombre del archivo solo si existía y fue inicializado
        return if (fileExists) fileName else null
    }

    fun isRecording() = recorder != null
}