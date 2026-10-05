package com.igrupos.conversiones

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject

@Immutable
data class ConverterValues(
    val lmin: String = "",
    val m3h: String = "",
    val gpm: String = "",
    val psi: String = "",
    val bar: String = "",
    val kpa: String = "",
    val mca: String = ""
)

enum class ConverterUnit { LMIN, M3H, GPM, PSI, BAR, KPA, MCA }

@HiltViewModel
class ConverterViewModel @Inject constructor() : ViewModel() {

    private val _values = MutableStateFlow(ConverterValues())
    val values: StateFlow<ConverterValues> = _values.asStateFlow()

    fun updateValue(unit: ConverterUnit, input: String) {
        val value = input.replace(",", ".").toDoubleOrNull() ?: return
        val (flowValue, pressureValue) = when (unit) {
            ConverterUnit.LMIN -> value to 0.0
            ConverterUnit.M3H -> value to 0.0
            ConverterUnit.GPM -> value to 0.0
            ConverterUnit.PSI -> 0.0 to value
            ConverterUnit.BAR -> 0.0 to value
            ConverterUnit.KPA -> 0.0 to value
            ConverterUnit.MCA -> 0.0 to value
        }

        _values.value = when (unit) {
            ConverterUnit.LMIN -> ConverterValues(
                lmin = formatValue(value),
                m3h = formatValue(value * 0.06),
                gpm = formatValue(value * 0.264172),
                psi = _values.value.psi,
                bar = _values.value.bar,
                kpa = _values.value.kpa,
                mca = _values.value.mca
            )
            ConverterUnit.M3H -> ConverterValues(
                lmin = formatValue(value / 0.06),
                m3h = formatValue(value),
                gpm = formatValue(value * 4.40287),
                psi = _values.value.psi,
                bar = _values.value.bar,
                kpa = _values.value.kpa,
                mca = _values.value.mca
            )
            ConverterUnit.GPM -> ConverterValues(
                lmin = formatValue(value / 0.264172),
                m3h = formatValue(value / 4.40287),
                gpm = formatValue(value),
                psi = _values.value.psi,
                bar = _values.value.bar,
                kpa = _values.value.kpa,
                mca = _values.value.mca
            )
            ConverterUnit.PSI -> ConverterValues(
                lmin = _values.value.lmin,
                m3h = _values.value.m3h,
                gpm = _values.value.gpm,
                psi = formatValue(value),
                bar = formatValue(value / 14.5038),
                kpa = formatValue(value * 6.89476),
                mca = formatValue(value / 1.4223)
            )
            ConverterUnit.BAR -> ConverterValues(
                lmin = _values.value.lmin,
                m3h = _values.value.m3h,
                gpm = _values.value.gpm,
                psi = formatValue(value * 14.5038),
                bar = formatValue(value),
                kpa = formatValue(value * 100),
                mca = formatValue(value * 10.1972)
            )
            ConverterUnit.KPA -> ConverterValues(
                lmin = _values.value.lmin,
                m3h = _values.value.m3h,
                gpm = _values.value.gpm,
                psi = formatValue(value / 6.89476),
                bar = formatValue(value / 100),
                kpa = formatValue(value),
                mca = formatValue(value / 9.80665)
            )
            ConverterUnit.MCA -> ConverterValues(
                lmin = _values.value.lmin,
                m3h = _values.value.m3h,
                gpm = _values.value.gpm,
                psi = formatValue(value * 1.4223),
                bar = formatValue(value / 10.1972),
                kpa = formatValue(value * 9.80665),
                mca = formatValue(value)
            )
        }
    }

    private fun formatValue(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", value)
        }
    }
}
