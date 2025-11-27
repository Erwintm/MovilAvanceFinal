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


    private lateinit var videoFile: File


    fun createVideoFileUri(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val videoFileName = "VID_${timeStamp}.mp4"


        videoFile = File(context.cacheDir, videoFileName)

        // Usa FileProvider para obtener una URI segura
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            videoFile
        )
    }


    fun getTempVideoFile(): File? {
        return if (::videoFile.isInitialized) videoFile else null
    }
}