package org.arabicchemistry.lab

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Normalized geometry only: relative bond lengths, no force-field or reaction prediction. */
data class SpatialAtom(val symbol: String, val x: Double, val y: Double, val z: Double)
data class MolecularShape(val formula: String, val nameAr: String, val nameEn: String, val angle: Double, val atoms: List<SpatialAtom>)
object MolecularGeometry {
    private val d = 1.0 / sqrt(3.0)
    val examples = listOf(
        MolecularShape("H₂O", "الماء • منحني", "Water • bent", 104.5, listOf(SpatialAtom("O",0.0,0.0,0.0), SpatialAtom("H",1.0,0.0,0.0), SpatialAtom("H",cos(Math.toRadians(104.5)),sin(Math.toRadians(104.5)),0.0))),
        MolecularShape("CO₂", "ثاني أكسيد الكربون • خطي", "Carbon dioxide • linear", 180.0, listOf(SpatialAtom("C",0.0,0.0,0.0),SpatialAtom("O",1.0,0.0,0.0),SpatialAtom("O",-1.0,0.0,0.0))),
        MolecularShape("CH₄", "الميثان • رباعي السطوح", "Methane • tetrahedral", Math.toDegrees(kotlin.math.acos(-1.0/3.0)), listOf(SpatialAtom("C",0.0,0.0,0.0),SpatialAtom("H",d,d,d),SpatialAtom("H",d,-d,-d),SpatialAtom("H",-d,d,-d),SpatialAtom("H",-d,-d,d)))
    )
}
