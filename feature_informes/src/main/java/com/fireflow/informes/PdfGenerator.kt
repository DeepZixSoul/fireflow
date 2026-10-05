package com.fireflow.informes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.fireflow.core.util.DateUtils
import com.fireflow.domain.model.Client
import com.fireflow.domain.model.Motor
import com.fireflow.domain.model.MotorType
import com.fireflow.domain.model.Photo
import com.fireflow.domain.model.PressureGroup
import com.fireflow.domain.model.PressureMeasurement
import com.fireflow.domain.model.Revision
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class PdfGenerator(
    private val context: Context
) {
    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN_LEFT = 40f
        private const val MARGIN_RIGHT = 555f
        private const val MARGIN_TOP = 50f
        private const val MARGIN_BOTTOM = 792f
        private const val LINE_HEIGHT = 18f
        private const val MAX_PHOTO_WIDTH = 200f
        private const val MAX_PHOTO_HEIGHT = 150f
    }

    private val titlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#D32F2F")
        textSize = 28f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val subtitlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1A1A1A")
        textSize = 18f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val bodyPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1A1A1A")
        textSize = 14f
    }

    private val labelPaint = Paint().apply {
        color = android.graphics.Color.GRAY
        textSize = 12f
    }

    private lateinit var document: PdfDocument
    private lateinit var page: PdfDocument.Page
    private lateinit var canvas: Canvas
    private var yPos = 0f

    fun generateReport(
        client: Client,
        group: PressureGroup,
        revision: Revision,
        motors: List<Motor>,
        measurements: Map<Long, PressureMeasurement?>,
        photos: List<Photo>
    ): File {
        document = PdfDocument()
        startNewPage()

        canvas.drawText("FireFlow - Informe de Revisión", MARGIN_LEFT, yPos, titlePaint)
        yPos += 40f

        canvas.drawLine(MARGIN_LEFT, yPos, MARGIN_RIGHT, yPos, Paint().apply {
            color = android.graphics.Color.parseColor("#D32F2F")
            strokeWidth = 3f
        })
        yPos += 20f

        drawClientSection(client)
        drawGroupSection(group)
        drawRevisionSection(revision)
        drawChecklistSection(revision)
        drawCurvesSection(motors, measurements, revision)
        drawPhotosSection(photos)

        document.finishPage(page)

        val dir = File(context.cacheDir, "reports")
        dir.mkdirs()
        val file = File(dir, "reporte_${revision.id}.pdf")

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    private fun startNewPage() {
        val pageNumber = if (::document.isInitialized) document.pages.size + 1 else 1
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        page = document.startPage(pageInfo)
        canvas = page.canvas
        yPos = MARGIN_TOP
    }

    private fun ensureSpace(needed: Float) {
        if (yPos + needed > MARGIN_BOTTOM) {
            document.finishPage(page)
            startNewPage()
        }
    }

    private fun drawSubtitle(text: String) {
        ensureSpace(30f)
        canvas.drawText(text, MARGIN_LEFT, yPos, subtitlePaint)
        yPos += 25f
    }

    private fun drawInfo(label: String, value: String) {
        ensureSpace(LINE_HEIGHT + 4f)
        canvas.drawText("$label: ", MARGIN_LEFT, yPos, labelPaint)
        canvas.drawText(value, MARGIN_LEFT + 100f, yPos, bodyPaint)
        yPos += LINE_HEIGHT
    }

    private fun drawWrappedText(label: String, value: String) {
        val availableWidth = (MARGIN_RIGHT - MARGIN_LEFT - 100f).toInt()
        val charsPerLine = bodyPaint.breakText(value, true, availableWidth.toFloat(), null)
        if (charsPerLine >= value.length) {
            drawInfo(label, value)
            return
        }
        ensureSpace(LINE_HEIGHT + 4f)
        canvas.drawText("$label: ", MARGIN_LEFT, yPos, labelPaint)
        var remaining = value
        while (remaining.isNotEmpty()) {
            ensureSpace(LINE_HEIGHT + 4f)
            val count = bodyPaint.breakText(remaining, true, availableWidth.toFloat(), null)
            val line = remaining.take(count)
            canvas.drawText(line, MARGIN_LEFT + 100f, yPos, bodyPaint)
            yPos += LINE_HEIGHT
            remaining = remaining.substring(count).trimStart()
        }
    }

    private fun drawClientSection(client: Client) {
        drawSubtitle("Datos del Cliente")
        drawInfo("Nombre", client.name)
        drawInfo("CIF", client.cif)
        drawInfo("Dirección", client.address)
        drawInfo("Provincia", client.province)
        drawInfo("Contacto", client.contactPerson)
        drawInfo("Teléfono", client.phone)
        yPos += 10f
    }

    private fun drawGroupSection(group: PressureGroup) {
        drawSubtitle("Datos del Grupo")
        drawInfo("Marca", group.brand)
        drawInfo("Modelo", group.model)
        drawInfo("Nº Serie", group.serialNumber)
        drawInfo("Potencia", group.power)
        yPos += 10f
    }

    private fun drawRevisionSection(revision: Revision) {
        drawSubtitle("Resultados de la Revisión")
        drawInfo("Fecha", DateUtils.formatDateTime(revision.date))
        drawInfo("Técnico", revision.technicianName)
        if (revision.notes.isNotBlank()) {
            drawWrappedText("Observaciones", revision.notes)
        }
        yPos += 10f
    }

    private fun drawChecklistSection(revision: Revision) {
        if (revision.checklistResults.isEmpty()) return

        drawSubtitle("Checklist")
        revision.checklistResults.forEach { (label, checked) ->
            val status = if (checked) "✓" else "✗"
            drawInfo(status, label)
        }
        yPos += 10f
    }

    private fun drawCurvesSection(
        motors: List<Motor>,
        measurements: Map<Long, PressureMeasurement?>,
        revision: Revision
    ) {
        val motorsWithData = motors.filter { motor ->
            val m = measurements[motor.id]
            m != null && (m.pressureAt0 != null || m.pressureAt50 != null ||
                m.pressureAt100 != null || m.pressureAt140 != null)
        }
        if (motorsWithData.isEmpty()) return

        val year = java.util.Calendar.getInstance().apply { timeInMillis = revision.date }
            .get(java.util.Calendar.YEAR)

        drawSubtitle("Curvas de rendimiento ($year)")

        motorsWithData.forEach { motor ->
            ensureSpace(LINE_HEIGHT + 20f)
            val motorType = if (motor.motorType == MotorType.ELECTRIC) "Eléctrico" else "Diésel"
            canvas.drawText("Motor $motorType", MARGIN_LEFT, yPos, bodyPaint)
            yPos += LINE_HEIGHT

            val measurement = measurements[motor.id] ?: return@forEach
            val rows = listOf(
                0 to measurement.pressureAt0,
                50 to measurement.pressureAt50,
                100 to measurement.pressureAt100,
                140 to measurement.pressureAt140
            )
            rows.forEach { (pct, pressure) ->
                if (pressure != null) {
                    val flow = motor.nominalFlow * pct / 100.0
                    val text = "$pct% — Caudal: ${formatFlow(flow)} L/min · Presión: ${formatPressure(pressure)} Bar"
                    drawInfo("  ", text)
                }
            }
            yPos += 6f
        }
        yPos += 10f
    }

    private fun drawPhotosSection(photos: List<Photo>) {
        if (photos.isEmpty()) return

        drawSubtitle("Fotografías (${photos.size})")

        photos.forEachIndexed { index, photo ->
            ensureSpace(MAX_PHOTO_HEIGHT + 40f)
            drawPhoto(photo, index)
        }
    }

    private fun drawPhoto(photo: Photo, index: Int) {
        try {
            val file = File(photo.filePath)
            if (!file.exists()) {
                canvas.drawText("Foto ${index + 1} - Archivo no encontrado", MARGIN_LEFT, yPos, labelPaint)
                yPos += 20f
                return
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap == null) {
                canvas.drawText("Foto ${index + 1} - Error de decodificación", MARGIN_LEFT, yPos, labelPaint)
                yPos += 20f
                return
            }
            val scaled = scaleBitmap(bitmap, MAX_PHOTO_WIDTH, MAX_PHOTO_HEIGHT)
            canvas.drawBitmap(scaled, MARGIN_LEFT, yPos, null)
            yPos += scaled.height + 5f
            canvas.drawText("Foto ${index + 1}", MARGIN_LEFT, yPos, labelPaint)
            yPos += 20f
            if (scaled != bitmap) bitmap.recycle()
            scaled.recycle()
        } catch (e: Exception) {
            canvas.drawText("Foto ${index + 1} - No disponible", MARGIN_LEFT, yPos, labelPaint)
            yPos += 20f
        }
    }

    private fun formatFlow(flow: Double): String =
        if (flow == flow.toLong().toDouble()) flow.toLong().toString()
        else String.format(Locale.US, "%.1f", flow)

    private fun formatPressure(pressure: Double): String =
        String.format(Locale.US, "%.2f", pressure)

    private fun scaleBitmap(source: Bitmap, maxWidth: Float, maxHeight: Float): Bitmap {
        val ratio = minOf(maxWidth / source.width, maxHeight / source.height)
        val newWidth = (source.width * ratio).toInt()
        val newHeight = (source.height * ratio).toInt()
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
    }
}
