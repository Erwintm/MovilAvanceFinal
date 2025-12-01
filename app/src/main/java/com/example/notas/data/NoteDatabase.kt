package com.example.notas.data

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [Note::class, Multimedia::class, Recordatorio::class], version = 7, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    abstract fun multimediaDao(): MultimediaDao
    abstract fun recordatorioDao(): RecordatorioDao
}