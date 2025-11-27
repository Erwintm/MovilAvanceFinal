package com.example.notas.utils

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.core.net.toUri
import java.io.File

class AudioPlayer(private val context: Context) {

    // Referencia al reproductor de audio.
    var player: ExoPlayer? = null
        private set

    init {
        // Inicializa el ExoPlayer cuando se crea la clase
        player = ExoPlayer.Builder(context).build()
    }

    /**
     * Prepara y comienza la reproducción de un archivo de audio local.
     */
    fun play(filePath: String) {
        // 1. Obtiene el archivo local desde el directorio de archivos de la app
        val file = File(context.filesDir, filePath)

        if (!file.exists()) {
            println("Archivo de audio no encontrado: $filePath")
            return
        }

        // 2. Prepara el MediaItem con la URI del archivo local
        val mediaItem = MediaItem.fromUri(file.toUri())

        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true
    }

    /**
     * Detiene la reproducción actual.
     */
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