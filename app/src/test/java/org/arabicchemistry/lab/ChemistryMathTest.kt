package org.arabicchemistry.lab

import kotlin.math.pow
import org.junit.Assert.*
import org.junit.Test

/** Physical invariants and boundaries, rather than copies of the model algorithms. */
class ChemistryMathTest {
    private fun rejects(block: () -> Unit) {
        assertThrows(IllegalArgumentException::class.java) { block() }
    }

    @Test fun waterConservesBothAtomTypesAndStopsAtALimitingReactant() {
        for (hydrogen in 0..50) for (oxygen in 0..50) {
            val result = ChemistryMath.water(hydrogen, oxygen)
            assertEquals("H atoms", 2 * hydrogen, 2 * result.water + 2 * result.hydrogenLeft)
            assertEquals("O atoms", 2 * oxygen, result.water + 2 * result.oxygenLeft)
            assertTrue(result.water >= 0 && result.hydrogenLeft >= 0 && result.oxygenLeft >= 0)
            assertEquals("whole reaction events produce pairs of water molecules", 0, result.water % 2)
            assertTrue("no further complete reaction remains", result.hydrogenLeft < 2 || result.oxygenLeft == 0)
        }
        val maximum = ChemistryMath.water(1000, 1000)
        assertEquals(ChemistryMath.WaterResult(1000, 0, 500), maximum)
    }

    @Test fun waterRejectsUnsupportedCountsAndPreservesOddHydrogen() {
        assertEquals(ChemistryMath.WaterResult(2, 1, 1), ChemistryMath.water(3, 2))
        assertEquals(ChemistryMath.WaterResult(0, 1, 4), ChemistryMath.water(1, 4))
        rejects { ChemistryMath.water(-1, 0) }
        rejects { ChemistryMath.water(0, -1) }
        rejects { ChemistryMath.water(1001, 1) }
        rejects { ChemistryMath.water(1, Int.MAX_VALUE) }
    }

    @Test fun dilutionConservesSoluteAndNeverIncreasesConcentration() {
        for (ratio in listOf(1.0, 2.0, 5.0, 1000.0)) {
            val result = ChemistryMath.dilution(1.5, 80.0, 80.0 * ratio)
            assertEquals(1.5 * 80.0, result * 80.0 * ratio, 1e-10)
            assertTrue(result in 0.0..1.5)
        }
        assertEquals(0.0, ChemistryMath.dilution(0.0, 1.0, 2.0), 0.0)
        assertEquals(Double.MAX_VALUE, ChemistryMath.dilution(Double.MAX_VALUE, 1.0, 1.0), 0.0)
        rejects { ChemistryMath.dilution(1.0, 0.0, 2.0) }
        rejects { ChemistryMath.dilution(1.0, 100.0, 99.0) }
    }

    @Test fun idealGasUsesKpaUnitsAndObeysProportionality() {
        val reference = ChemistryMath.gasPressureKpa(1.0, 273.15, 22.414)
        assertEquals(101.325, reference, 0.002)
        assertEquals(2 * reference, ChemistryMath.gasPressureKpa(2.0, 273.15, 22.414), 1e-10)
        assertEquals(reference / 2, ChemistryMath.gasPressureKpa(1.0, 273.15, 44.828), 1e-10)
        assertEquals(2 * reference, ChemistryMath.gasPressureKpa(1.0, 546.30, 22.414), 1e-10)
        assertEquals(0.0, ChemistryMath.gasPressureKpa(0.0, 300.0, 1.0), 0.0)
        rejects { ChemistryMath.gasPressureKpa(1.0, 0.0, 1.0) }
        rejects { ChemistryMath.gasPressureKpa(1.0, 300.0, 0.0) }
        rejects { ChemistryMath.gasPressureKpa(Double.MAX_VALUE, 300.0, 1.0) }
        rejects { ChemistryMath.gasPressureKpa(1.0, Double.MAX_VALUE, Double.MIN_VALUE) }
    }

    @Test fun acidIncludesWaterAndSatisfiesChargeBalance() {
        assertEquals(7.0, ChemistryMath.acidPh(0.0), 1e-12)
        assertEquals(3.0, ChemistryMath.acidPh(0.001), 1e-8)
        val concentrations = listOf(0.0, 1e-12, 1e-9, 1e-6, 1e-3, 0.1)
        val ph = concentrations.map(ChemistryMath::acidPh)
        assertTrue(ph.zipWithNext().all { (previous, next) -> next < previous })
        for (c in concentrations) {
            val hydrogen = 10.0.pow(-ChemistryMath.acidPh(c))
            assertEquals("electroneutrality including OH-", c, hydrogen - 1e-14 / hydrogen, 1e-12)
        }
        rejects { ChemistryMath.acidPh(-1e-9) }
        rejects { ChemistryMath.acidPh(0.10001) }
    }

    @Test fun containedGoldHasConsistentUnitsAndRecoveryBounds() {
        assertEquals(ChemistryMath.GoldResult(20.0, 12.0), ChemistryMath.gold(10.0, 2.0, 60.0))
        assertEquals(ChemistryMath.GoldResult(20.0, 0.0), ChemistryMath.gold(10.0, 2.0, 0.0))
        assertEquals(ChemistryMath.GoldResult(20.0, 20.0), ChemistryMath.gold(10.0, 2.0, 100.0))
        assertEquals(ChemistryMath.GoldResult(0.0, 0.0), ChemistryMath.gold(0.0, 5.0, 100.0))
        assertEquals(Double.MAX_VALUE, ChemistryMath.gold(Double.MAX_VALUE, 1.0, 100.0).recoveredGrams, 0.0)
        rejects { ChemistryMath.gold(1.0, 2.0, 100.001) }
        rejects { ChemistryMath.gold(1.0, 2.0, -1.0) }
        rejects { ChemistryMath.gold(Double.MAX_VALUE, 2.0, 50.0) }
    }

    @Test fun modelInputsRejectNonfiniteAndNegativeNumbers() {
        for (bad in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -1.0)) {
            rejects { ChemistryMath.dilution(bad, 1.0, 2.0) }
            rejects { ChemistryMath.dilution(1.0, bad, 2.0) }
            rejects { ChemistryMath.dilution(1.0, 1.0, bad) }
            rejects { ChemistryMath.gasPressureKpa(bad, 300.0, 1.0) }
            rejects { ChemistryMath.gasPressureKpa(1.0, bad, 1.0) }
            rejects { ChemistryMath.gasPressureKpa(1.0, 300.0, bad) }
            rejects { ChemistryMath.acidPh(bad) }
            rejects { ChemistryMath.gold(bad, 1.0, 50.0) }
            rejects { ChemistryMath.gold(1.0, bad, 50.0) }
            rejects { ChemistryMath.gold(1.0, 1.0, bad) }
        }
    }

    @Test fun numberInputSupportsArabicPersianAndScientificForms() {
        assertEquals(12.5, ChemistryMath.parseNumber(" ١٢٫٥ ")!!, 0.0)
        assertEquals(12.5, ChemistryMath.parseNumber("۱۲,۵")!!, 0.0)
        assertEquals(-2.5, ChemistryMath.parseNumber("−٢٫٥")!!, 0.0)
        assertEquals(0.001, ChemistryMath.parseNumber("١e-٣")!!, 0.0)
        for (invalid in listOf("", " ", "NaN", "Infinity", "-Infinity", "1e309", "١٬٠٠٠", "1,234,567", "2 mol")) {
            assertNull(invalid, ChemistryMath.parseNumber(invalid))
        }
    }
}
