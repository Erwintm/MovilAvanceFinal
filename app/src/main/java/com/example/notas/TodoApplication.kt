package com.example.notas

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.Room
import com.example.notas.data.NoteDatabase
import com.example.notas.data.NoteRepository
import com.example.notas.data.OfflineNoteRepository
import com.example.notas.data.RecordatorioRepository

class TodoApplication : Application() {

    lateinit var database: NoteDatabase
    lateinit var repository: NoteRepository
    lateinit var recordatorioRepository: RecordatorioRepository

    override fun onCreate() {
        super.onCreate()

        // Crear base de datos
        database = Room.databaseBuilder(
            applicationContext,
            NoteDatabase::class.java,
            "notes_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        repository = OfflineNoteRepository(
            database.noteDao(),
            database.multimediaDao(),
            database.recordatorioDao()
        )

        recordatorioRepository = RecordatorioRepository(
            database.recordatorioDao()
        )

        // Crear canal de notificación
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "recordatorios_channel",
                "Alarmas y Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Canal para recordatorios programados"

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
