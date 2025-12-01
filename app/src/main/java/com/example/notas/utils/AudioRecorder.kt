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


class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null


    private lateinit var outputFile: File



    fun createAudioFileUri(): Uri {

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val audioFileName = "AUD_${timeStamp}.mp4"


        val audioFile = File(context.cacheDir, audioFileName)


        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            audioFile
        )
    }


    fun start(fileName: String) {

        outputFile = File(context.filesDir, fileName)


        stopAndRelease()


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

                prepare()

                start()
            } catch (e: Exception) {

                e.printStackTrace()

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