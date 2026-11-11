package org.arabicchemistry.lab

import kotlin.math.log10
import kotlin.math.sqrt

/** Ideal educational models. They do not prescribe laboratory operations. */
object ChemistryMath {
    data class WaterResult(val water: Int, val hydrogenLeft: Int, val oxygenLeft: Int)
    data class GoldResult(val containedGrams: Double, val recoveredGrams: Double)

    private fun valid(vararg values: Double) {
        require(values.all { it.isFinite() && it >= 0.0 }) { "finite_nonnegative" }
    }
    private fun finite(value: Double): Double {
        require(value.isFinite()) { "overflow" }
        return value
    }
    fun water(hydrogen: Int, oxygen: Int): WaterResult {
        require(hydrogen in 0..1000 && oxygen in 0..1000)
        val extent = minOf(hydrogen / 2, oxygen)
        return WaterResult(2 * extent, hydrogen - 2 * extent, oxygen - extent)
    }
    fun dilution(concentration: Double, initialMl: Double, finalMl: Double): Double {
        valid(concentration, initialMl, finalMl)
        require(initialMl > 0 && finalMl >= initialMl) { "dilution_volume" }
        return finite(concentration * (initialMl / finalMl))
    }
    fun gasPressureKpa(moles: Double, kelvin: Double, litres: Double): Double {
        valid(moles, kelvin, litres)
        require(kelvin > 0 && litres > 0) { "positive_temperature_volume" }
        return finite(8.314462618 * (moles / litres) * kelvin)
    }
    /** Strong monoprotic acid, ideal dilute solution at 25 C, including water autoionization. */
    fun acidPh(molar: Double): Double {
        valid(molar)
        require(molar <= 0.1) { "dilute_acid_only" }
        val hydrogen = (molar + sqrt(molar * molar + 4e-14)) / 2
        return -log10(hydrogen)
    }
    fun gold(tonnes: Double, gramsPerTonne: Double, recoveryPercent: Double): GoldResult {
        valid(tonnes, gramsPerTonne, recoveryPercent)
        require(recoveryPercent <= 100) { "recovery_range" }
        val contained = finite(tonnes * gramsPerTonne)
        return GoldResult(contained, finite(contained * (recoveryPercent / 100)))
    }
    /** Arabic/Persian numerals and decimal marks; no grouping separators. */
    fun parseNumber(text: String): Double? {
        val normalized = text.trim().map { c -> when(c) {
            in '٠'..'٩' -> '0' + (c - '٠')
            in '۰'..'۹' -> '0' + (c - '۰')
            '٫', ',' -> '.'
            '−' -> '-'
            else -> c
        } }.joinToString("")
        return normalized.toDoubleOrNull()?.takeIf { it.isFinite() }
    }
}
