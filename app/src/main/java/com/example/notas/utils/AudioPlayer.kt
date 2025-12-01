package com.example.notas.utils

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.core.net.toUri
import java.io.File

/**
 * Clase que gestiona la reproducción de archivos de audio locales
 * utilizando la librería ExoPlayer (parte de Media3).
 */
class AudioPlayer(private val context: Context) {


    var player: ExoPlayer? = null
        private set

    init {

        player = ExoPlayer.Builder(context).build()
    }


    fun play(filePath: String) {


        val file = File(context.filesDir, filePath)


        if (!file.exists()) {
            println("Archivo de audio no encontrado: $filePath")
            return
        }


        val mediaItem = MediaItem.fromUri(file.toUri())


        player?.setMediaItem(mediaItem)

        player?.prepare()

        player?.playWhenReady = true
    }


    fun stop() {

        player?.stop()

        player?.clearMediaItems()
    }


    fun release() {

        player?.release()

        player = null
    }


    fun isPlaying(): Boolean {

        return player?.isPlaying ?: false
    }
}