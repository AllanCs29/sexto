package com.example.routineapp.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
    import android.provider.MediaStore
import com.example.routineapp.data.DayHistory
import com.example.routineapp.data.RoutineItem
import java.io.OutputStream
import java.time.LocalDate

object PdfExporter {
    fun exportToday(ctx: Context, items: List<RoutineItem>): Boolean {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val c: Canvas = page.canvas
        val p = Paint().apply { textSize = 14f }
        var y = 40f
        p.isFakeBoldText = true
        c.drawText("RoutineApp - " + LocalDate.now().toString(), 40f, y, p)
        p.isFakeBoldText = false
        y += 20f
        items.forEach {
            val line = "${it.time ?: "—"}  ${it.title}${if (it.done) " ✓" else ""}"
            c.drawText(line, 40f, y, p); y += 18f
        }
        pdf.finishPage(page)
        return savePdf(ctx, pdf, "Routine_${LocalDate.now()}.pdf")
    }

    fun exportWeekly(ctx: Context, history: List<DayHistory>): Boolean {
        val last7 = history.takeLast(7)
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val c: Canvas = page.canvas
        val p = Paint().apply { textSize = 14f }
        var y = 40f
        p.isFakeBoldText = true
        c.drawText("RoutineApp - Resumen semanal", 40f, y, p)
        p.isFakeBoldText = false; y += 24f
        if (last7.isEmpty()) {
            c.drawText("Sin datos.", 40f, y, p)
        } else {
            last7.forEach { d ->
                val pct = if (d.total==0) 0 else (100 * d.done / d.total)
                c.drawText("${d.date}: ${d.done}/${d.total}  (${pct}%)", 40f, y, p); y += 18f
            }
            y += 10f
            val sumDone = last7.sumOf { it.done }
            val sumTotal = last7.sumOf { it.total }
            c.drawText("Total semanal: $sumDone/$sumTotal", 40f, y, p)
        }
        pdf.finishPage(page)
        return savePdf(ctx, pdf, "Routine_Weekly_${LocalDate.now()}.pdf")
    }

    private fun savePdf(ctx: Context, pdf: PdfDocument, name: String): Boolean {
        val resolver = ctx.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, name)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
        }
        val uri = if (Build.VERSION.SDK_INT >= 29) {
            resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        } ?: return false

        resolver.openOutputStream(uri)?.use { out ->
            pdf.writeTo(out)
        } ?: return false
        pdf.close()
        return true
    }
}
