package com.example.routineapp.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class RoutineItem(val title: String, val time: String? = null, val done: Boolean = false)
data class Exercise(val name: String, val sets: Int, val reps: Int, val doneSets: Int = 0)
data class DayHistory(val date: String, val done: Int, val total: Int)

private const val PREFS = "routine_ultra_prefs"
private const val KEY_ITEMS = "items"
private const val KEY_EX = "exercises"
private const val KEY_HISTORY = "history"
private const val KEY_THEME_DARK = "themeDark"
private const val KEY_THEME_VARIANT = "themeVariant"

fun loadItems(ctx: Context): List<RoutineItem> {
    val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ITEMS, null) ?: return emptyList()
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            RoutineItem(o.getString("title"), o.optString("time").ifEmpty { null }, o.optBoolean("done"))
        }
    } catch (_: Exception) { emptyList() }
}

fun saveItems(ctx: Context, list: List<RoutineItem>) {
    val arr = JSONArray()
    list.forEach {
        val o = JSONObject(); o.put("title", it.title); o.put("time", it.time); o.put("done", it.done); arr.put(o)
    }
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ITEMS, arr.toString()).apply()
}

fun loadExercises(ctx: Context): List<Exercise> {
    val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_EX, null) ?: return emptyList()
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Exercise(o.getString("name"), o.getInt("sets"), o.getInt("reps"), o.optInt("doneSets",0))
        }
    } catch (_: Exception) { emptyList() }
}

fun saveExercises(ctx: Context, list: List<Exercise>) {
    val arr = JSONArray()
    list.forEach {
        val o = JSONObject(); o.put("name", it.name); o.put("sets", it.sets); o.put("reps", it.reps); o.put("doneSets", it.doneSets); arr.put(o)
    }
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_EX, arr.toString()).apply()
}

fun appendHistory(ctx: Context, done: Int, total: Int) {
    val sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val raw = sp.getString(KEY_HISTORY, "[]")!!
    val arr = JSONArray(raw)
    val today = LocalDate.now().toString()
    // keep all but today
    val keep = JSONArray()
    for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        if (o.getString("date") != today) keep.put(o)
    }
    val o = JSONObject()
    o.put("date", today); o.put("done", done); o.put("total", total)
    keep.put(o)
    sp.edit().putString(KEY_HISTORY, keep.toString()).apply()
}

fun loadHistory(ctx: Context): List<DayHistory> {
    val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_HISTORY, "[]")!!
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            DayHistory(o.getString("date"), o.optInt("done",0), o.optInt("total",0))
        }
    } catch (_: Exception) { emptyList() }
}

fun getThemeDark(ctx: Context): Boolean =
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_THEME_DARK, false)

fun setThemeDark(ctx: Context, dark: Boolean) =
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_THEME_DARK, dark).apply()

fun getThemeVariant(ctx: Context): Int =
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_THEME_VARIANT, 0)

fun setThemeVariant(ctx: Context, variant: Int) =
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_THEME_VARIANT, variant).apply()
