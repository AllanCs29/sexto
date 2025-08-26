package com.example.routineapp.util

import android.content.Context
import androidx.work.*
import com.example.routineapp.data.loadItems
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    fun scheduleToday(ctx: Context) {
        val list = loadItems(ctx)
        val now = System.currentTimeMillis()
        list.forEachIndexed { i, it ->
            val t = it.time ?: return@forEachIndexed
            val lt = runCatching { LocalTime.parse(t) }.getOrNull() ?: return@forEachIndexed
            val trigger = lt.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val delay = trigger - now
            if (delay > 0) {
                val req = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf("t" to it.title, "h" to t, "id" to i))
                    .build()
                WorkManager.getInstance(ctx).enqueue(req)
            }
        }
    }
}

class ReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val t = inputData.getString("t") ?: "Actividad"
        val h = inputData.getString("h") ?: ""
        val id = inputData.getInt("id", 0)
        Notifier.notify(applicationContext, "Recordatorio", "$t a las $h", id + 1000)
        return Result.success()
    }
}
