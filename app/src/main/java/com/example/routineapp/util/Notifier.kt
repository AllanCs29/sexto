package com.example.routineapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.routineapp.R

private const val CHANNEL_ID = "routine_channel"

private fun ensureChannel(ctx: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Routine", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }
}

fun Notifier_notify(ctx: Context, title: String, text: String, id: Int) {
    ensureChannel(ctx)
    val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val n = NotificationCompat.Builder(ctx, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification) // crea un vector simple si no lo tienes
        .setContentTitle(title)
        .setContentText(text)
        .setAutoCancel(true)
        .build()
    nm.notify(id, n)
}
