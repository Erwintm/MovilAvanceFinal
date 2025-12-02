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
        }

        val requestCode = System.currentTimeMillis().toInt()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
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

        val channelId = "recordatorios_channel"


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                ctx,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) return
        }

        val manager =
            ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
