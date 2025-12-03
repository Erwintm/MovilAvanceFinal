package com.example.notas.alarmas

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.notas.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import android.app.NotificationChannel
import com.example.notas.MainActivity
import com.example.notas.TodoApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


data class AlarmItem(
    val noteId: Int,
    val alarmTime: LocalDateTime,
    val title: String,
    val description: String
)


interface AlarmScheduler {
    fun schedule(alarm: AlarmItem)
    fun cancel(alarm: AlarmItem)
}

class AlarmSchedulerImpl(private val context: Context) : AlarmScheduler {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun schedule(alarm: AlarmItem) {

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TITLE", alarm.title)
            putExtra("DESC", alarm.description)
            putExtra("NOTE_ID", alarm.noteId)
        }



        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.noteId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        // Convertir LocalDateTime a epochMillis
        val epochMillis = alarm.alarmTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("AlarmScheduler", "No se pueden programar alarmas exactas.")
                return
            }
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                epochMillis,
                pendingIntent
            )
            Log.i("AlarmScheduler", "Alarma programada para: $epochMillis")
        } catch (ex: SecurityException) {
            Log.e("AlarmScheduler", "SecurityException", ex)
        }
    }

    override fun cancel(alarm: AlarmItem) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.noteId,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val ctx = context ?: return

        val title = intent?.getStringExtra("TITLE") ?: "Recordatorio"
        val desc = intent?.getStringExtra("DESC") ?: ""
        val noteId = intent?.getIntExtra("NOTE_ID", -1) ?: -1

        val channelId = "recordatorios_channel"

        //  Intent para abrir la app y navegar a la nota
        val clickIntent = Intent(ctx, MainActivity::class.java).apply {
            putExtra("NOTE_ID", noteId)
            putExtra("FROM_NOTIFICATION", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val clickPendingIntent = PendingIntent.getActivity(
            ctx,
            noteId,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(clickPendingIntent)   //abrir
            .build()

        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(noteId, notification)
    }
}
class BootReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            val app = context.applicationContext as TodoApplication
            val repo = app.recordatorioRepository
            val alarmScheduler = AlarmSchedulerImpl(context)

            CoroutineScope(Dispatchers.IO).launch {

                val recordatorios = repo.getAllRecordatoriosDirect()

                val now = LocalDateTime.now()

                recordatorios.forEach { rec ->

                    val dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(rec.fechaRecordatorio),
                        ZoneId.systemDefault()
                    )


                    if (dateTime.isAfter(now)) {
                        alarmScheduler.schedule(
                            AlarmItem(
                                noteId = rec.notaId,
                                alarmTime = dateTime,
                                title = rec.titulo,
                                description = rec.descripcion
                            )
                        )
                    }
                }
            }
        }
    }
}



