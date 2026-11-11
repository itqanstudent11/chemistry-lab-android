package org.arabicchemistry.lab

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

class MolecularGeometryTest {
    @Test fun bondVectorsHaveUnitLengthAndReferenceAngles() {
        MolecularGeometry.examples.forEach { shape ->
            assertEquals(0.0, shape.atoms.first().x, 0.0)
            assertEquals(0.0, shape.atoms.first().y, 0.0)
            assertEquals(0.0, shape.atoms.first().z, 0.0)
            val bonds = shape.atoms.drop(1)
            bonds.forEach { assertEquals(1.0, sqrt(it.x*it.x+it.y*it.y+it.z*it.z), 1e-12) }
            for (i in bonds.indices) for(j in i+1 until bonds.size) {
                val a=bonds[i];val b=bonds[j]
                val angle=Math.toDegrees(acos((a.x*b.x+a.y*b.y+a.z*b.z).coerceIn(-1.0,1.0)))
                assertEquals(shape.formula,shape.angle,angle,1e-10)
            }
        }
    }
    @Test fun methaneIsSpatialAndCarbonDioxideIsLinear() {
        val methane=MolecularGeometry.examples.single { it.formula=="CH₄" }
        assertEquals(4,methane.atoms.count { it.symbol=="H" })
        assertTrue(methane.atoms.any { it.z>0 } && methane.atoms.any { it.z<0 })
        assertEquals(180.0,MolecularGeometry.examples.single { it.formula=="CO₂" }.angle,0.0)
        assertEquals(104.5,MolecularGeometry.examples.single { it.formula=="H₂O" }.angle,0.0)
    }
}
