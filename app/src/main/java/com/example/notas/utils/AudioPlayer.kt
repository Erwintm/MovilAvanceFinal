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

    // Referencia al reproductor de audio principal.
    // 'private set' significa que solo esta clase puede cambiar la referencia del objeto 'player'.
    var player: ExoPlayer? = null
        private set

    init {
        // Bloque de inicialización: se ejecuta inmediatamente al crear un objeto AudioPlayer.
        // Inicializa el ExoPlayer usando el Context provisto.
        player = ExoPlayer.Builder(context).build()
    }

    /**
     * Prepara y comienza la reproducción de un archivo de audio local.
     * @param filePath El nombre del archivo de audio (que está guardado en filesDir).
     */
    fun play(filePath: String) {

        // 1. Obtiene el objeto File local desde el directorio de archivos internos de la app (filesDir).
        // Los audios y videos se almacenan en 'context.filesDir' para almacenamiento persistente y privado.
        val file = File(context.filesDir, filePath)

        // Verificación de seguridad: si el archivo no existe, imprime un error y sale.
        if (!file.exists()) {
            println("Archivo de audio no encontrado: $filePath")
            return
        }

        // 2. Prepara el MediaItem: Convierte el objeto File en una URI, que es el formato
        // que ExoPlayer necesita para identificar el contenido.
        val mediaItem = MediaItem.fromUri(file.toUri())

        // Carga el recurso (el archivo de audio) en el reproductor.
        player?.setMediaItem(mediaItem)
        // Prepara el reproductor para la reproducción (carga y decodifica el audio).
        player?.prepare()
        // Indica que la reproducción debe comenzar tan pronto como esté lista.
        player?.playWhenReady = true
    }

    /**
     * Detiene la reproducción actual.
     */
    fun stop() {
        // Detiene el proceso de reproducción.
        player?.stop()
        // Limpia la lista de archivos cargados en el reproductor.
        player?.clearMediaItems()
    }

    /**
     * Libera los recursos del reproductor.
     * ESTO ES CRUCIAL: Debe llamarse cuando el reproductor ya no se necesita (ej. al salir de la pantalla)
     * para liberar memoria y recursos de hardware (como decodificadores).
     */
    fun release() {
        // Libera los recursos del ExoPlayer.
        player?.release()
        // Anula la referencia para evitar fugas de memoria.
        player = null
    }

    /**
     * Devuelve el estado de reproducción.
     * @return True si el audio se está reproduciendo actualmente.
     */
    fun isPlaying(): Boolean {
        // Devuelve el valor de 'isPlaying'. Si 'player' es nulo, devuelve false.
        return player?.isPlaying ?: false
    }
}