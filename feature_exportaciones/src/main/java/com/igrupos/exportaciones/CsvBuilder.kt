package com.igrupos.exportaciones

import com.igrupos.core.util.DateUtils
import com.igrupos.domain.model.Client
import com.igrupos.domain.model.Motor
import com.igrupos.domain.model.MotorType
import com.igrupos.domain.model.PressureGroup
import com.igrupos.domain.model.PressureMeasurement
import com.igrupos.domain.model.Revision
import java.util.Locale

/**
 * Construye el contenido CSV de una revisión.
 * Separador ';' y BOM UTF-8 para que Excel en español lo abra correctamente.
 */
object CsvBuilder {

    const val BOM = "\uFEFF"
    private const val SEP = ";"

    fun build(
        client: Client,
        group: PressureGroup,
        revision: Revision,
        motors: List<Motor>,
        measurements: Map<Long, PressureMeasurement?>
    ): String {
        val sb = StringBuilder(BOM)

        sb.appendLine("CLIENTE")
        sb.appendLine(csvRow("Nombre", client.name))
        sb.appendLine(csvRow("CIF", client.cif))
        sb.appendLine(csvRow("Dirección", client.address))
        sb.appendLine(csvRow("Provincia", client.province))
        sb.appendLine(csvRow("Contacto", client.contactPerson))
        sb.appendLine(csvRow("Teléfono", client.phone))
        sb.appendLine(csvRow("Email", client.email))
        sb.appendLine()

        sb.appendLine("GRUPO DE PRESIÓN")
        sb.appendLine(csvRow("Marca", group.brand))
        sb.appendLine(csvRow("Modelo", group.model))
        sb.appendLine(csvRow("Nº Serie", group.serialNumber))
        sb.appendLine(csvRow("Potencia", group.power))
        sb.appendLine()

        sb.appendLine("REVISIÓN")
        sb.appendLine(csvRow("Fecha", DateUtils.formatDateTime(revision.date)))
        sb.appendLine(csvRow("Técnico", revision.technicianName))
        if (revision.notes.isNotBlank()) {
            sb.appendLine(csvRow("Observaciones", revision.notes))
        }
        sb.appendLine()

        if (revision.checklistResults.isNotEmpty()) {
            sb.appendLine("CHECKLIST")
            sb.appendLine(csvRow("Elemento", "Cumplido"))
            revision.checklistResults.forEach { (label, checked) ->
                sb.appendLine(csvRow(label, if (checked) "Sí" else "No"))
            }
            sb.appendLine()
        }

        val motorsWithData = motors.filter { motor ->
            val m = measurements[motor.id]
            m != null && (m.pressureAt0 != null || m.pressureAt50 != null ||
                m.pressureAt100 != null || m.pressureAt140 != null)
        }
        if (motorsWithData.isNotEmpty()) {
            sb.appendLine("CURVAS DE RENDIMIENTO")
            sb.appendLine(csvRow("Motor", "% Caudal", "Caudal (L/min)", "Presión (Bar)"))
            motorsWithData.forEach { motor ->
                val motorLabel = if (motor.motorType == MotorType.ELECTRIC) "Eléctrico" else "Diésel"
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
                        sb.appendLine(
                            csvRow(
                                motorLabel,
                                "$pct%",
                                formatNumber(flow, 1),
                                formatNumber(pressure, 2)
                            )
                        )
                    }
                }
            }
        }

        return sb.toString()
    }

    private fun csvRow(vararg fields: String): String =
        fields.joinToString(SEP) { escape(it) }

    private fun escape(field: String): String {
        val needsQuotes = field.contains(SEP) || field.contains('"') ||
            field.contains('\n') || field.contains('\r')
        return if (needsQuotes) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }

    private fun formatNumber(value: Double, decimals: Int): String =
        String.format(Locale.US, "%.${decimals}f", value)
}
