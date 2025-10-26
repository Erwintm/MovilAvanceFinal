package com.example.notas

import android.app.Application
import androidx.room.Room
import com.example.notas.data.NoteDatabase
import com.example.notas.data.NoteRepository

class TodoApplication : Application() {
    lateinit var database: NoteDatabase
    lateinit var repository: NoteRepository

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            NoteDatabase::class.java,
            "notes_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        repository = NoteRepository(database.noteDao())
    }
}
