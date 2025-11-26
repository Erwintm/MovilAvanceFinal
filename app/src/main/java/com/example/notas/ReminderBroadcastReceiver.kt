package com.example.notas

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderBroadcastReceiver : BroadcastReceiver() {
    private val CHANNEL_ID = "recordatorio_channel"
    private val CHANNEL_NAME = "Recordatorios de Notas"

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        // 1. Crear el canal de notificación si es necesario (API 26+)
        createNotificationChannel(context)

        // 2. Obtener los datos pasados por la alarma
        val title = intent.getStringExtra("TITLE") ?: "Recordatorio de Nota"
        val desc = intent.getStringExtra("DESC") ?: "¡Hora de tu tarea o nota pendiente!"

        // Usamos el ID de la alarma (action) como ID de la notificación para que sea única
        val notificationId = intent.action?.toIntOrNull() ?: System.currentTimeMillis().toInt()

        // 3. Construir la notificación
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            // 🚨 SOLUCIÓN RÁPIDA: Usar un ícono de recurso de Android que existe en todos los dispositivos
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title) // 🚨 SOLUCIONADO
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // 4. Mostrar la notificación (El permiso POST_NOTIFICATIONS ahora se pide en MainActivity)
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Canal para notificaciones de recordatorios de notas."
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}