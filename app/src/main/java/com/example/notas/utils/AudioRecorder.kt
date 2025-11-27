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

    // Usamos 'lateinit' para referenciar el archivo de destino.
    private lateinit var outputFile: File


    /**
     * Crea un archivo temporal con un nombre único para almacenar el audio grabado
     * y devuelve su Uri compatible con FileProvider.
     * Esto permite usar ActivityResultContracts.CaptureVideo() para grabar audio.
     */
    fun createAudioFileUri(): Uri {

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val audioFileName = "AUD_${timeStamp}.mp4"


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

                prepare()
                start()
            } catch (e: Exception) {

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

            recorder?.stop()
            fileExists = if (::outputFile.isInitialized) outputFile.exists() else false
        } catch (e: RuntimeException) {

            e.printStackTrace()
        } finally {

            stopAndRelease()
        }


        return if (fileExists) fileName else null
    }


    private fun stopAndRelease() {
        recorder?.release()
        recorder = null
    }

    fun isRecording() = recorder != null
}