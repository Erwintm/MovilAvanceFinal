package com.example.notas.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class VideoRecorder(private val context: Context) {


    private lateinit var videoFile: File



    fun createVideoFileUri(): Uri {

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        val videoFileName = "VID_${timeStamp}.mp4"


        videoFile = File(context.cacheDir, videoFileName)


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