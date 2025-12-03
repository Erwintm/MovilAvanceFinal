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
        // Libera los recursos asociados al MediaRecorder.
        recorder?.release()
        // Anula la referencia del objeto.
        recorder = null
    }


    fun isRecording() = recorder != null
}