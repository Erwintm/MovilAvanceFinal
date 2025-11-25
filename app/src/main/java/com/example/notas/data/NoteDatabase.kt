package com.example.notas.data

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [Note::class, Multimedia::class], version = 3, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    abstract fun multimediaDao(): MultimediaDao
}