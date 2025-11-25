package com.example.notas

import android.app.Application
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
        database = Room.databaseBuilder(
            applicationContext,
            NoteDatabase::class.java,
            "notes_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        repository = OfflineNoteRepository(database.noteDao(),
            database.multimediaDao(),
            database.recordatorioDao())
        recordatorioRepository = RecordatorioRepository(
            database.recordatorioDao()
        )
    }
}
