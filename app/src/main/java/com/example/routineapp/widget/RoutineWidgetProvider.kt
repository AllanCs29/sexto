package com.example.routineapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.routineapp.R
import com.example.routineapp.data.loadItems
import com.example.routineapp.data.saveItems

class RoutineWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_MARK_NEXT -> {
                val list = loadItems(context).toMutableList()
                val idx = list.indexOfFirst { !it.done }
                if (idx >= 0) list[idx] = list[idx].copy(done = true)
                saveItems(context, list)
                updateAll(context)
            }
            ACTION_REFRESH -> updateAll(context)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id ->
            val views = buildViews(context)
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    private fun updateAll(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(android.content.ComponentName(context, RoutineWidgetProvider::class.java))
        onUpdate(context, mgr, ids)
    }

    private fun buildViews(context: Context): RemoteViews {
        val items = loadItems(context)
        val done = items.count { it.done }
        val total = items.size
        val next = items.firstOrNull { !it.done }?.let { "${it.time ?: ""} ${it.title}" } ?: "✔ Todo listo"

        val views = RemoteViews(context.packageName, R.layout.widget_routine)
        views.setTextViewText(R.id.title, "RoutineApp")
        views.setTextViewText(R.id.next, "Siguiente: $next")
        views.setTextViewText(R.id.progress, "Progreso: $done/$total")

        val doneIntent = Intent(context, RoutineWidgetProvider::class.java).apply { action = ACTION_MARK_NEXT }
        val donePI = PendingIntent.getBroadcast(context, 0, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.btn_done, donePI)

        val refIntent = Intent(context, RoutineWidgetProvider::class.java).apply { action = ACTION_REFRESH }
        val refPI = PendingIntent.getBroadcast(context, 1, refIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.btn_refresh, refPI)

        return views
    }

    companion object {
        const val ACTION_MARK_NEXT = "com.example.routineapp.ACTION_MARK_NEXT"
        const val ACTION_REFRESH = "com.example.routineapp.ACTION_REFRESH"
    }
}
