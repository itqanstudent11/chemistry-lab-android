package org.arabicchemistry.lab

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable internal fun MoleculeViewer() {
    val s = LocalWords.current
    var index by rememberSaveable { mutableIntStateOf(0) }
    var yaw by rememberSaveable { mutableFloatStateOf(0.4f) }
    var pitch by rememberSaveable { mutableFloatStateOf(0.3f) }
    val molecule = MolecularGeometry.examples[index]
    val title = s.text(molecule.nameAr, molecule.nameEn)
    val angle = String.format(Locale.US, "%.2f", molecule.angle)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Heading(s.text("الهندسة الجزيئية ثلاثية الأبعاد", "3D molecular geometry"))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MolecularGeometry.examples.forEachIndexed { i, shape -> FilterChip(index == i, { index = i; yaw = 0.4f; pitch = 0.3f }, label = { Text(shape.formula) }) }
        }
        Text("${molecule.formula} • $title")
        Text(s.text("اسحب النموذج لتدويره، أو استخدم الأزرار.", "Drag the model to rotate it, or use the buttons."))
        Canvas(Modifier.fillMaxWidth().height(260.dp).background(Color(0xFFE2F0FA))
            .semantics { contentDescription = "${molecule.formula} $title $angle°" }
            .pointerInput(Unit) { detectDragGestures { change, drag ->
                change.consume(); yaw = (yaw + drag.x * .008f) % 6.283185f; pitch = (pitch + drag.y * .008f).coerceIn(-1.4f, 1.4f)
            } }) {
            val scale = size.minDimension * .32f
            val rotated = molecule.atoms.map { atom ->
                val x = atom.x * cos(yaw) + atom.z * sin(yaw)
                val z = -atom.x * sin(yaw) + atom.z * cos(yaw)
                val y = atom.y * cos(pitch) - z * sin(pitch)
                val depth = atom.y * sin(pitch) + z * cos(pitch)
                Triple(Offset(center.x + x.toFloat()*scale, center.y - y.toFloat()*scale), depth.toFloat(), atom.symbol)
            }
            rotated.drop(1).forEach { drawLine(Color(0xFF64849A), rotated[0].first, it.first, strokeWidth = 7.dp.toPx()) }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; textSize = 16.dp.toPx(); color = android.graphics.Color.BLACK }
            rotated.sortedBy { it.second }.forEach { (p, depth, symbol) ->
                val r = (18f + depth*3f).dp.toPx()
                val color = when(symbol) { "O" -> Color(0xFFFF968E); "C" -> Color(0xFFB5BBC5); else -> Color(0xFFB5DCF5) }
                drawCircle(Color(0xFF406079), r + 1.dp.toPx(), p)
                drawCircle(color, r, p)
                drawContext.canvas.nativeCanvas.drawText(symbol, p.x, p.y - (paint.ascent()+paint.descent())/2, paint)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { yaw = (yaw + .523599f) % 6.283185f }) { Text(s.text("تدوير 30°", "Rotate 30°")) }
            TextButton(onClick = { yaw = .4f; pitch = .3f }) { Text(s.text("إعادة المنظور", "Reset view")) }
        }
        InfoCard(s.text("زاوية الرابطة وحدود النموذج", "Bond angle and model limits"), s.text(
            "الزاوية المرجعية: $angle°. إحداثيات مثالية نسبية، وزاوية الماء تقريبية للجزيء الحر. أطوال الروابط وأنصاف أقطار الذرات ليست على مقياس حقيقي. يعرض الشكل الفراغي ولا يحسب حركة جزيئية أو طاقة أو تفاعلًا. في CO₂ تمثل القضبان اتجاه الروابط، لا رتبتها.",
            "Reference angle: $angle°. Ideal relative coordinates; water's free-molecule angle is approximate. Bond lengths and atomic radii are not to physical scale. This displays spatial geometry without molecular dynamics, energy or reaction prediction. CO₂ rods indicate bond directions, not bond order."))
        SourceLink(s.text("مرجع الأشكال الجزيئية • OpenStax", "Molecular structure reference • OpenStax"), "https://openstax.org/books/chemistry-2e/pages/7-6-molecular-structure-and-polarity")
    }
}
