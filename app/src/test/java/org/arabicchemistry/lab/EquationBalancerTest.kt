package org.arabicchemistry.lab

import org.junit.Assert.*
import org.junit.Test

class EquationBalancerTest {
    private val known=setOf("H","O","Fe","C","Ca","P","Al","S","Na","Cl")
    @Test fun canonicalIntegerSolutionsConserveAtoms() {
        val cases=mapOf("H2+O2->H2O" to listOf(2,1,2), "Fe+O2=Fe2O3" to listOf(4,3,2), "C2H6+O2->CO2+H2O" to listOf(2,7,4,6), "Ca(OH)2+H3PO4->Ca3(PO4)2+H2O" to listOf(3,2,1,6), "Al+HCl->AlCl3+H2" to listOf(2,6,2,3))
        cases.forEach { (equation,expected) ->
            val result=EquationBalancer.balance(equation,known)
            assertEquals(equation,expected,result.coefficients)
            val totals=mutableMapOf<String,Int>()
            (result.reactants+result.products).forEachIndexed { i,f ->
                EquationBalancer.atoms(f,known).forEach { (symbol,n) -> totals[symbol]=(totals[symbol] ?: 0)+n*result.coefficients[i]*if(i<result.reactants.size) 1 else -1 }
            }
            assertTrue(totals.values.all { it==0 })
        }
        assertEquals(listOf(2,1,2),EquationBalancer.balance("H₂ + O₂ → H₂O",known).coefficients)
    }
    @Test fun rejectsInvalidAmbiguousOrUnsupportedEquations() {
        listOf("H2->O2","H2+O2->H2O+H2O2","Xx+O2->XxO2","H0+O2->H2O","H2+->H2O","Ca(OH2->CaO","2H2+O2->H2O","H2->H2->H2","Na++Cl-->NaCl").forEach { equation ->
            assertThrows(equation,IllegalArgumentException::class.java) { EquationBalancer.balance(equation,known) }
        }
    }
}
