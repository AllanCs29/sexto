@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.routineapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.routineapp.data.*
import com.example.routineapp.ui.theme.RoutineTheme
import com.example.routineapp.ui.theme.ThemeVariant
import com.example.routineapp.util.Notifier
import com.example.routineapp.util.PdfExporter
import com.example.routineapp.util.PomodoroService
import com.example.routineapp.util.ReminderScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

enum class Tab { HOY, PESAS, FUTBOL, ESTUDIO, STATS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var dark by remember { mutableStateOf(getThemeDark(this)) }
            var variant by remember {
                mutableStateOf(
                    ThemeVariant.values()[getThemeVariant(this)
                        .coerceIn(0, ThemeVariant.values().size - 1)]
                )
            }
            RoutineTheme(variant = variant, dark = dark) {
                val ctx = this
                var tab by remember { mutableStateOf(Tab.HOY) }
                var items by remember { mutableStateOf(loadItems(ctx)) }
                var ex by remember { mutableStateOf(loadExercises(ctx)) }

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("RoutineApp", fontWeight = FontWeight.Bold)
                                    Text(
                                        LocalDate.now().toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            },
                            actions = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Switch claro/oscuro
                                    Icon(imageVector = Icons.Outlined.DarkMode, contentDescription = null)
                                    Switch(
                                        checked = !dark,
                                        onCheckedChange = { isLight ->
                                            dark = !isLight; setThemeDark(ctx, dark)
                                        }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    // Paleta: Olive / Arena / Carbón (Segmented)
                                    var selected by remember { mutableIntStateOf(variant.ordinal) }
                                    SingleChoiceSegmentedButtonRow {
                                        ThemeVariant.values().forEachIndexed { i, v ->
                                            SegmentedButton(
                                                selected = selected == i,
                                                onClick = {
                                                    selected = i
                                                    variant = ThemeVariant.values()[i]
                                                    setThemeVariant(ctx, i)
                                                },
                                                label = { Text(v.label) },
                                                icon = {}
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    // Acciones rápidas (PDF Hoy / PDF Semana)
                                    IconButton(onClick = {
                                        val ok = PdfExporter.exportToday(ctx, items)
                                        Notifier.notify(ctx, "Exportar", if (ok) "PDF de hoy exportado" else "Error al exportar", 100)
                                    }) { Icon(Icons.Outlined.PictureAsPdf, "PDF Hoy") }
                                    IconButton(onClick = {
                                        val ok = PdfExporter.exportWeekly(ctx, loadHistory(ctx))
                                        Notifier.notify(ctx, "Exportar", if (ok) "PDF semanal exportado" else "Error al exportar", 101)
                                    }) { Icon(Icons.Outlined.Assessment, "PDF Semana") }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        // Tabs con iconos
                        NavigationBar {
                            NavTab(tab, Tab.HOY, Icons.Outlined.Today, "Hoy") { tab = it }
                            NavTab(tab, Tab.PESAS, Icons.Outlined.FitnessCenter, "Pesas") { tab = it }
                            NavTab(tab, Tab.FUTBOL, Icons.Outlined.SportsSoccer, "Fútbol") { tab = it }
                            NavTab(tab, Tab.ESTUDIO, Icons.Outlined.School, "Estudio") { tab = it }
                            NavTab(tab, Tab.STATS, Icons.Outlined.Assessment, "Stats") { tab = it }
                        }
                    }
                ) { inner ->
                    Column(
                        Modifier
                            .padding(inner)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxSize()
                    ) {
                        when (tab) {
                            Tab.HOY -> TodayTab(
                                items = items,
                                onToggle = { idx, checked ->
                                    items = items.toMutableList().also { it[idx] = it[idx].copy(done = checked) }
                                },
                                onAdd = { title, time -> items = items + RoutineItem(title, time, false) },
                                onGenerate = { items = generateTodayPlan() },
                                onSave = {
                                    saveItems(ctx, items)
                                    ReminderScheduler.scheduleToday(ctx)
                                    val done = items.count { it.done }
                                    appendHistory(ctx, done, items.size)
                                }
                            )

                            Tab.PESAS -> WeightsTab(
                                exList = ex.ifEmpty { defaultWeightsPlan() },
                                onUpdate = { ex = it; saveExercises(ctx, it) }
                            )

                            Tab.FUTBOL -> FootballTab()

                            Tab.ESTUDIO -> StudyTabWithPomodoro(
                                startService = { text ->
                                    ctx.startService(Intent(ctx, PomodoroService::class.java).putExtra("text", text))
                                },
                                stopService = { ctx.stopService(Intent(ctx, PomodoroService::class.java)) }
                            )

                            Tab.STATS -> StatsTab(history = loadHistory(ctx))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavTab(
    current: Tab,
    value: Tab,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: (Tab) -> Unit
) {
    NavigationBarItem(
        selected = current == value,
        onClick = { onClick(value) },
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) }
    )
}

/* ---------- HOY (UI rediseñada) ---------- */

@Composable
fun TodayTab(
    items: List<RoutineItem>,
    onToggle: (Int, Boolean) -> Unit,
    onAdd: (String, String?) -> Unit,
    onGenerate: () -> Unit,
    onSave: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var sortAsc by remember { mutableStateOf(true) }

    // Barra de acciones
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Actividad") },
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Hora HH:mm") },
            modifier = Modifier.width(160.dp)
        )
    }
    Spacer(Modifier.height(12.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(onClick = {
            if (title.isNotBlank()) {
                onAdd(title.trim(), time.ifBlank { null }); title = ""; time = ""
            }
        }) { Icon(Icons.Outlined.Bolt, null); Spacer(Modifier.width(6.dp)); Text("Agregar") }

        FilledTonalButton(onClick = onGenerate) {
            Icon(Icons.Outlined.Refresh, null); Spacer(Modifier.width(6.dp)); Text("Generar HOY")
        }

        Button(onClick = onSave) {
            Icon(Icons.Outlined.Save, null); Spacer(Modifier.width(6.dp)); Text("Guardar + Notificar")
        }
    }
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Buscar") },
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        FilterChip(
            selected = sortAsc,
            onClick = { sortAsc = !sortAsc },
            label = { Text(if (sortAsc) "Hora ↑" else "Hora ↓") }
        )
    }
    Spacer(Modifier.height(12.dp))

    val display = items
        .filter { it.title.contains(search, ignoreCase = true) || search.isBlank() }
        .sortedWith(compareBy<RoutineItem> {
            it.time?.let { t -> runCatching { LocalTime.parse(t) }.getOrNull() }
        }.let { cmp -> if (sortAsc) cmp else cmp.reversed() })

    val done = items.count { it.done }
    val progress = if (items.isEmpty()) 0f else done.toFloat() / items.size
    Text("Progreso", style = MaterialTheme.typography.labelMedium)
    LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(6.dp))
    Text("$done / ${items.size} completadas", fontWeight = FontWeight.SemiBold)

    Spacer(Modifier.height(8.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(display) { _, it ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(it.time ?: "—", modifier = Modifier.width(64.dp), fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Checkbox(
                        checked = it.done,
                        onCheckedChange = { c ->
                            val index = items.indexOf(it)
                            if (index >= 0) onToggle(index, c)
                        }
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(it.title)
                    Spacer(Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFF6B7D57)
                    )
                }
            }
        }
    }
}

/* ---------- Datos por defecto y otras pestañas ---------- */

fun generateTodayPlan(): List<RoutineItem> {
    val dow = LocalDate.now().dayOfWeek
    val list = mutableListOf<RoutineItem>()
    list += RoutineItem("Levantarse", "07:00")
    list += RoutineItem("Trabajo", "08:00")
    val pesas = when (dow) {
        DayOfWeek.MONDAY -> "Pesas: Empuje (Pecho/Hombro/Tríceps)"
        DayOfWeek.TUESDAY -> "Pesas: Piernas (Cuádriceps/Glúteo)"
        DayOfWeek.WEDNESDAY -> "Pesas: Tirón (Espalda/Bíceps)"
        DayOfWeek.THURSDAY -> "Pesas: Full Body ligero"
        DayOfWeek.FRIDAY -> "Pesas: Core + movilidad"
        else -> null
    }
    pesas?.let { list += RoutineItem(it, "16:00") }
    list += RoutineItem("Estudio programación (2h)", "17:15")
    list += RoutineItem("Fútbol: rondos/decisiones 20-30m", "21:00")
    list += RoutineItem("Higiene / Ordenar cuarto", "22:00")
    return list
}

@Composable
fun WeightsTab(exList: List<Exercise>, onUpdate: (List<Exercise>) -> Unit) {
    Text("Rutina del día (mancuernas/barra)", fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(exList) { idx, e ->
            ElevatedCard {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(e.name, fontWeight = FontWeight.SemiBold)
                        Text("${e.sets} x ${e.reps} reps", style = MaterialTheme.typography.labelMedium)
                    }
                    AssistChip(onClick = {
                        val up = exList.toMutableList()
                        up[idx] = e.copy(doneSets = (e.doneSets + 1).coerceAtMost(e.sets))
                        onUpdate(up)
                    }, label = { Text("+ set  ${e.doneSets}/${e.sets}") })
                }
            }
        }
    }
}

fun defaultWeightsPlan(): List<Exercise> {
    val dow = LocalDate.now().dayOfWeek
    return when (dow) {
        DayOfWeek.MONDAY -> listOf(
            Exercise("Press banca mancuernas", 4, 8),
            Exercise("Elevaciones laterales", 3, 12),
            Exercise("Fondos en banco", 3, 12),
            Exercise("Flexiones", 3, 15),
        )
        DayOfWeek.TUESDAY -> listOf(
            Exercise("Sentadilla goblet", 4, 10),
            Exercise("Zancadas", 3, 12),
            Exercise("Puente de glúteo", 3, 15),
        )
        DayOfWeek.WEDNESDAY -> listOf(
            Exercise("Remo mancuerna", 4, 10),
            Exercise("Curl bíceps alterno", 3, 12),
            Exercise("Face pull banda", 3, 15),
        )
        DayOfWeek.THURSDAY -> listOf(
            Exercise("Peso muerto rumano", 4, 8),
            Exercise("Press militar", 3, 10),
            Exercise("Plancha", 3, 45),
        )
        DayOfWeek.FRIDAY -> listOf(
            Exercise("Rueda abdominal / Hollow", 4, 12),
            Exercise("Farmer walk (pasos)", 4, 40),
            Exercise("Hip hinge movilidad", 3, 10),
        )
        else -> listOf(Exercise("Circuito movilidad", 3, 10))
    }
}

@Composable
fun FootballTab() {
    Text("Fútbol — Toma de decisiones", fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    val drills = listOf(
        "Rondos 4v2: orientación corporal (2x6')",
        "1v1 cierre: temporización (2x5')",
        "Juego posicional 3 zonas (2x8')",
        "Primer control + pase (3x8')"
    )
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(drills) { _, d ->
            ElevatedCard { Text(d, modifier = Modifier.padding(14.dp)) }
        }
    }
}

@Composable
fun StudyTabWithPomodoro(
    startService: (String) -> Unit,
    stopService: () -> Unit
) {
    val ctx = LocalContext.current
    Text("Estudio — Pomodoro", fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))

    var work by remember { mutableStateOf(50) }
    var rest by remember { mutableStateOf(10) }
    var remaining by remember { mutableStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var onWork by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }

    fun start() {
        if (running) return
        running = true
        val startSec = (if (onWork) work else rest) * 60
        remaining = if (remaining > 0) remaining else startSec
        startService(if (onWork) "Pomodoro: Trabajando" else "Pomodoro: Descanso")
        job = scope.launch {
            while (remaining > 0 && running) {
                delay(1000); remaining -= 1
            }
            if (remaining <= 0) {
                running = false; onWork = !onWork; remaining = 0
                stopService()
                Notifier.notify(
                    ctx,
                    if (onWork) "Descanso terminado" else "Bloque terminado",
                    if (onWork) "¡A estudiar!" else "Toma un descanso",
                    200
                )
            }
        }
    }
    fun pause() { running = false; job?.cancel(); stopService() }
    fun reset() { running = false; job?.cancel(); remaining = 0; onWork = true; stopService() }

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            work.toString(), { v -> v.toIntOrNull()?.let { work = it.coerceIn(5, 120) } },
            label = { Text("Trabajo (min)") }, modifier = Modifier.width(150.dp)
        )
        Spacer(Modifier.width(12.dp))
        OutlinedTextField(
            rest.toString(), { v -> v.toIntOrNull()?.let { rest = it.coerceIn(1, 60) } },
            label = { Text("Descanso (min)") }, modifier = Modifier.width(150.dp)
        )
    }
    Spacer(Modifier.height(10.dp))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { start() }) { Text(if (running) "Continuar" else "Iniciar") }
        FilledTonalButton(onClick = { pause() }) { Text("Pausar") }
        OutlinedButton(onClick = { reset() }) { Text("Reset") }
        val mins = remaining / 60; val secs = remaining % 60
        Spacer(Modifier.width(8.dp))
        Text("Tiempo: %02d:%02d".format(mins, secs), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatsTab(history: List<DayHistory>) {
    val last14 = history.takeLast(14)
    val last7 = history.takeLast(7)
    if (last14.isEmpty()) {
        Text("Sin datos aún. Guarda tu día para empezar a ver progreso.")
        return
    }
    Text("Progreso — Línea (14 días)", fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Canvas(Modifier.fillMaxWidth().height(160.dp)) {
        if (last14.size >= 2) {
            val step = size.width / (last14.size - 1)
            var prevX = 0f
            var prevY = size.height * (1f - (last14[0].let { if (it.total == 0) 0f else it.done.toFloat() / it.total }))
            for (i in 1 until last14.size) {
                val pct = last14[i].let { if (it.total == 0) 0f else it.done.toFloat() / it.total }
                val x = i * step
                val y = size.height * (1f - pct)
                drawLine(
                    Color(0xFF6B7D57),
                    androidx.compose.ui.geometry.Offset(prevX, prevY),
                    androidx.compose.ui.geometry.Offset(x, y),
                    strokeWidth = 6f
                )
                prevX = x; prevY = y
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    Text("Progreso — Semanal", fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    val sumDone = last7.sumOf { it.done }
    val sumTotal = last7.sumOf { it.total }
    Canvas(Modifier.fillMaxWidth().height(180.dp)) {
        val r = minOf(size.width, size.height) / 2.5f
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        val sweepDone = if (sumTotal == 0) 0f else 360f * (sumDone.toFloat() / sumTotal)
        drawArc(
            Color(0xFF6B7D57),
            startAngle = -90f, sweepAngle = sweepDone, useCenter = true,
            topLeft = center - androidx.compose.ui.geometry.Offset(r, r),
            size = androidx.compose.ui.geometry.Size(2 * r, 2 * r)
        )
        drawArc(
            Color(0xFFB0B3AD),
            startAngle = -90f + sweepDone, sweepAngle = 360f - sweepDone, useCenter = true,
            topLeft = center - androidx.compose.ui.geometry.Offset(r, r),
            size = androidx.compose.ui.geometry.Size(2 * r, 2 * r)
        )
    }
}
