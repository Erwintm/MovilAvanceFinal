package com.example.notas.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null

    // El archivo de destino donde se guardará el audio
    private lateinit var outputFile: File

    fun start(fileName: String) {
        // 1. Crear el archivo de salida
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Usar FileDescriptor para manejar mejor el archivo de salida
                setOutputFile(FileOutputStream(outputFile).fd)
            } else {
                @Suppress("DEPRECATION")
                setOutputFile(outputFile.absolutePath)
            }

            try {
                prepare()
                start()
            } catch (e: Exception) {
                e.printStackTrace()
                recorder = null // En caso de error, liberamos la instancia
            }
        }
    }

    // Detiene y libera el MediaRecorder, devolviendo la ruta del archivo.
    fun stop(): String? {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        // Devolver el nombre del archivo para que la BD lo almacene (uriArchivo)
        return if (outputFile.exists()) outputFile.name else null
    }

    fun isRecording() = recorder != null
}