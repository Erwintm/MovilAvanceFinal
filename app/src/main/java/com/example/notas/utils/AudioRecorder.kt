package com.example.notas.utils

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Clase utilitaria para gestionar la grabación de audio usando el MediaRecorder de Android.
 * Se encarga de iniciar, detener la grabación y gestionar el archivo de salida.
 */
class AudioRecorder(private val context: Context) {
    // Referencia al objeto principal de grabación de Android. Es nullable.
    private var recorder: MediaRecorder? = null

    // Usamos 'lateinit' para almacenar la referencia al archivo PERMANENTE donde se guardará el audio.
    private lateinit var outputFile: File


    /**
     * NOTA: Esta función 'createAudioFileUri' ya no se usa en el flujo actual de audio.
     * En el flujo actual, la grabación se maneja INTERNAMENTE (start/stop) y el archivo
     * se guarda DIRECTAMENTE en filesDir, no se usa una URI para una app externa.
     * Se mantiene por si se quisiera grabar audio usando una app de cámara externa (como se hacía con el video).
     */
    fun createAudioFileUri(): Uri {

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val audioFileName = "AUD_${timeStamp}.mp4"

        // Crea el archivo DENTRO del directorio de caché (Temporal)
        val audioFile = File(context.cacheDir, audioFileName)

        // Usa FileProvider para obtener una URI segura.
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            audioFile
        )
    }

    /**
     * Prepara el MediaRecorder y comienza la grabación del audio.
     * @param fileName El nombre final del archivo (ej: AUD_20251127_093000.mp4).
     */
    fun start(fileName: String) {
        // 1. Crear el archivo de salida:
        // El archivo se crea DIRECTAMENTE en el directorio de archivos permanentes (filesDir).
        outputFile = File(context.filesDir, fileName)

        // Limpiar cualquier instancia anterior fallida antes de empezar (seguridad).
        stopAndRelease()

        // 2. Inicializar el MediaRecorder de forma segura para diferentes versiones de Android.
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Usa el constructor moderno de MediaRecorder (requerido desde Android S).
            MediaRecorder(context)
        } else {
            // Usa el constructor antiguo (deprecado pero necesario para versiones anteriores).
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        recorder?.apply {
            // 3. Configuración del MediaRecorder

            // Especifica la fuente de audio: el micrófono del dispositivo.
            setAudioSource(MediaRecorder.AudioSource.MIC)
            // Especifica el formato de salida: MPEG_4 (un contenedor común para audio/video).
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            // Especifica el codificador de audio: AAC (un códec de alta calidad).
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            // Asigna la ruta de salida, usando el path del archivo permanente que creamos.
            @Suppress("DEPRECATION")
            setOutputFile(outputFile.absolutePath)

            // 4. Iniciar la grabación
            try {
                // Prepara el grabador para la captura de audio.
                prepare()
                // Comienza la captura de audio.
                start()
            } catch (e: Exception) {
                // Manejo de errores (ej. si el micrófono está ocupado o falló la configuración).
                e.printStackTrace()
                // Si falla, se libera inmediatamente para no dejar recursos bloqueados.
                stopAndRelease()
            }
        }
    }

    /**
     * Detiene y libera el MediaRecorder de forma segura.
     * @return El nombre del archivo si la grabación fue exitosa, o null si falló.
     */
    fun stop(): String? {
        // Guarda el nombre del archivo ANTES de liberarlo, en caso de que sea necesario devolverlo.
        val fileName = if (::outputFile.isInitialized) outputFile.name else null
        var fileExists = false

        try {
            // Detiene la grabación. Esto asegura que el archivo se finalice y se pueda acceder.
            recorder?.stop()
            // Verifica si el archivo existe en el disco después de detener la grabación.
            fileExists = if (::outputFile.isInitialized) outputFile.exists() else false
        } catch (e: RuntimeException) {
            // Captura una excepción común si stop() es llamado cuando el grabador no está en el estado correcto.
            e.printStackTrace()
        } finally {
            // 5. Liberación de Recursos
            // Se asegura de que los recursos se liberen SIEMPRE, incluso si hubo una excepción.
            stopAndRelease()
        }

        // Si el archivo existe (es decir, la grabación funcionó), devuelve el nombre para guardarlo en la DB.
        return if (fileExists) fileName else null
    }


    /**
     * Función privada para liberar los recursos del sistema de grabación.
     * Es crucial para evitar fugas de memoria y liberar el micrófono.
     */
    private fun stopAndRelease() {
        // Libera los recursos asociados al MediaRecorder.
        recorder?.release()
        // Anula la referencia del objeto.
        recorder = null
    }

    /**
     * Devuelve el estado de grabación.
     * @return True si 'recorder' no es nulo, indicando que una sesión de grabación está activa.
     */
    fun isRecording() = recorder != null
}