package com.example.routineapp.util

import android.app.Service
import android.content.Intent
import android.os.IBinder

class PomodoroService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val text = intent?.getStringExtra("text") ?: "Pomodoro en curso"
        Notifier.ongoing(this, "RoutineApp", text, 555)
        return START_STICKY
    }
    override fun onDestroy() {
        Notifier.cancel(this, 555)
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
