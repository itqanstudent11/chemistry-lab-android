package org.arabicchemistry.lab

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

internal class Mineral(private val o: JSONObject) {
    val id: String = o.getString("id")
    val cat: String = o.getString("cat")
    val formula: String = o.getString("formula")
    val color: String = o.optString("color", "#888888")
    val habit: String = o.optString("habit", "massive")
    val mohs: String = o.optString("mohs")
    val density: String = o.optString("density")
    val hazard: Int = o.optInt("hazard", 0)
    fun t(key: String, english: Boolean): String = o.optString(key + if (english) "En" else "Ar")
}

internal object MineralRepository {
    fun load(context: Context): List<Mineral> {
        val arr = JSONArray(context.assets.open("minerals.json").bufferedReader().use { it.readText() })
        return (0 until arr.length()).map { Mineral(arr.getJSONObject(it)) }
    }
}

private val categoryKeys = listOf("all", "native", "ore", "rock", "ind", "gem")

@Composable
private fun categoryLabel(key: String): String {
    val s = LocalWords.current
    return when (key) {
        "native" -> s.text("معادن عنصرية", "Native elements")
        "ore" -> s.text("خامات فلزية", "Metal ores")
        "rock" -> s.text("مكوّنة للصخور", "Rock-forming")
        "ind" -> s.text("صناعية", "Industrial")
        "gem" -> s.text("أحجار كريمة وزينة", "Gems and ornamental")
        else -> s.text("الكل", "All")
    }
}

@Composable
internal fun MineralsScreen(onOpen: (String) -> Unit) {
    val s = LocalWords.current
    val context = LocalContext.current
    val minerals = remember { runCatching { MineralRepository.load(context) }.getOrDefault(emptyList()) }
    var query by rememberSaveable { mutableStateOf("") }
    var cat by rememberSaveable { mutableStateOf("all") }
    val q = query.trim()
    val visible = minerals.filter { m ->
        (cat == "all" || m.cat == cat) &&
            (q.isEmpty() || listOf(m.t("name", false), m.t("name", true), m.formula).any { it.contains(q, ignoreCase = true) })
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Heading(s.text("موسوعة المعادن والخامات", "Minerals and ores encyclopedia"))
                Text(
                    s.text("${minerals.size} معدنًا وخامًا برسوم توضيحية وخصائص وتكوّن واستخدامات وسلامة.",
                        "${minerals.size} minerals and ores with illustrations, properties, formation, uses and safety."),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            InfoCard(
                s.text("مقياس موس للصلادة", "Mohs hardness scale"),
                s.text("1 تلك • 2 جبس • 3 كالسيت • 4 فلوريت • 5 أباتيت • 6 أورثوكلاز • 7 كوارتز • 8 توباز • 9 كورندوم • 10 ماس. المعدن الأعلى يخدش الأدنى.",
                    "1 talc • 2 gypsum • 3 calcite • 4 fluorite • 5 apatite • 6 orthoclase • 7 quartz • 8 topaz • 9 corundum • 10 diamond. A higher mineral scratches a lower one.")
            )
        }
        item {
            OutlinedTextField(
                value = query, onValueChange = { query = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                label = { Text(s.text("ابحث بالاسم أو الصيغة", "Search by name or formula")) }
            )
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categoryKeys.forEach { c ->
                    FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(categoryLabel(c)) })
                }
            }
        }
        if (visible.isEmpty()) {
            item { Text(s.text("لا توجد نتائج مطابقة.", "No matching results."), style = MaterialTheme.typography.bodyLarge) }
        }
        items(visible.chunked(2), key = { it.first().id }) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { m -> MineralCard(m, Modifier.weight(1f)) { onOpen(m.id) } }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MineralCard(m: Mineral, modifier: Modifier, onClick: () -> Unit) {
    val s = LocalWords.current
    Card(
        onClick = onClick, modifier = modifier, shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            MineralArt(m.color, m.habit, Modifier.fillMaxWidth().height(112.dp))
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(m.t("name", s.english), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2)
                Text(m.formula, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                HazardTag(m.hazard)
            }
        }
    }
}

@Composable
private fun HazardTag(level: Int) {
    val s = LocalWords.current
    val scheme = MaterialTheme.colorScheme
    val label: String
    val bg: Color
    val fg: Color
    when (level) {
        2 -> { label = s.text("خطر سُمّي", "Toxic hazard"); bg = scheme.errorContainer; fg = scheme.onErrorContainer }
        1 -> { label = s.text("احتياطات سلامة", "Safety care"); bg = scheme.tertiaryContainer; fg = scheme.onTertiaryContainer }
        else -> { label = s.text("منخفض الخطورة", "Low hazard"); bg = scheme.secondaryContainer; fg = scheme.onSecondaryContainer }
    }
    Text(
        label, style = MaterialTheme.typography.labelSmall, color = fg,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

@Composable
internal fun MineralDetailScreen(id: String) {
    val s = LocalWords.current
    val context = LocalContext.current
    val m = remember(id) { runCatching { MineralRepository.load(context) }.getOrDefault(emptyList()).find { it.id == id } }
    if (m == null) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(s.text("تعذر العثور على المعدن.", "Mineral not found."))
        }
    } else {
        val en = s.english
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    MineralArt(m.color, m.habit, Modifier.fillMaxWidth().height(240.dp))
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(m.t("name", en), style = MaterialTheme.typography.headlineSmall)
                    Text(m.formula, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    HazardTag(m.hazard)
                }
            }
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(s.text("الخصائص الفيزيائية", "Physical properties"), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        PropRow(s.text("النظام البلوري", "Crystal system"), m.t("system", en))
                        PropRow(s.text("الصلادة (موس)", "Hardness (Mohs)"), m.mohs)
                        PropRow(s.text("الكثافة (غ/سم³)", "Density (g/cm³)"), m.density)
                        PropRow(s.text("اللمعان", "Luster"), m.t("luster", en))
                        PropRow(s.text("لون الخط", "Streak"), m.t("streak", en))
                        PropRow(s.text("اللون", "Colour"), m.t("col", en))
                    }
                }
            }
            item { InfoCard(s.text("نبذة", "Overview"), m.t("desc", en)) }
            item { InfoCard(s.text("التكوّن والتواجد", "Formation and occurrence"), m.t("form", en)) }
            item { InfoCard(s.text("الاستخدامات", "Uses"), m.t("uses", en)) }
            item { InfoCard(s.text("كيف تميّزه", "How to identify it"), m.t("tips", en)) }
            if (m.t("note", en).isNotBlank()) {
                item { InfoCard(s.text("سياق محلي وحدود المعلومات", "Local context and limits"), m.t("note", en)) }
            }
            item {
                val generic = when (m.hazard) {
                    2 -> s.text("مادة عالية الخطورة؛ لا تطحنها ولا تسخّنها ولا تتذوقها، واغسل يديك، وأبقها مغلقة بعيدًا عن الأطفال، وتخلّص منها وفق اللوائح.",
                        "A high-hazard material; never grind, heat or taste it, wash your hands, keep it sealed away from children and dispose of it according to regulations.")
                    1 -> s.text("تعامل بحذر: اقطع وطحن بالماء، وتجنب استنشاق الغبار، واغسل يديك بعد التعامل مع العينات.",
                        "Handle with care: cut and grind wet, avoid breathing dust and wash your hands after handling samples.")
                    else -> s.text("منخفض الخطورة في التداول العادي، لكن اغسل يديك وتجنب غبار الطحن.",
                        "Low hazard in normal handling, but wash your hands and avoid grinding dust.")
                }
                val extra = m.t("safe", en)
                InfoCard(s.text("السلامة", "Safety"), if (extra.isBlank()) generic else generic + "\n\n" + extra)
            }
            item {
                Text(
                    s.text("الرسم توضيحي مبسّط لشكل البلورة ولونها وليس صورة فوتوغرافية، ولا يكفي وحده لتعريف عينة؛ للتعرف على عينة استخدم الصلادة والخط واللمعان والكثافة أو مختبرًا معتمدًا.",
                        "The drawing is a simplified illustration of crystal form and colour, not a photograph, and is not enough to identify a sample; use hardness, streak, luster and density, or an accredited laboratory."),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PropRow(label: String, value: String) {
    Column {
        HorizontalDivider(Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(label, Modifier.weight(0.4f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, Modifier.weight(0.6f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
internal fun MineralArt(colorHex: String, habit: String, modifier: Modifier = Modifier) {
    val base = remember(colorHex) { runCatching { Color(android.graphics.Color.parseColor(colorHex)) }.getOrDefault(Color.Gray) }
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val a = minOf(w, h) * 0.34f
        val light = lerp(base, Color.White, 0.45f)
        val dark = lerp(base, Color.Black, 0.35f)
        val edge = Color.Black.copy(alpha = 0.28f)
        drawRect(
            Brush.radialGradient(colors = listOf(base.copy(alpha = 0.30f), Color.Transparent), center = Offset(cx, cy), radius = maxOf(w, h) * 0.6f)
        )
        fun poly(c: Color, p: List<Offset>) {
            val path = Path()
            path.moveTo(p[0].x, p[0].y)
            for (i in 1 until p.size) path.lineTo(p[i].x, p[i].y)
            path.close()
            drawPath(path, c)
            drawPath(path, edge, style = Stroke(width = 2f))
        }
        fun column(x: Float, yTop: Float, yBot: Float, half: Float, tip: Float) {
            val l = x - half
            val m1 = x - half * 0.3f
            val m2 = x + half * 0.4f
            val r = x + half
            poly(dark, listOf(Offset(l, yTop), Offset(m1, yTop), Offset(m1, yBot), Offset(l, yBot)))
            poly(base, listOf(Offset(m1, yTop), Offset(m2, yTop), Offset(m2, yBot), Offset(m1, yBot)))
            poly(light, listOf(Offset(m2, yTop), Offset(r, yTop), Offset(r, yBot), Offset(m2, yBot)))
            if (tip > 0f) {
                val ty = yTop - tip
                poly(dark, listOf(Offset(l, yTop), Offset(m1, yTop), Offset(x, ty)))
                poly(light, listOf(Offset(m1, yTop), Offset(m2, yTop), Offset(x, ty)))
                poly(base, listOf(Offset(m2, yTop), Offset(r, yTop), Offset(x, ty)))
            }
        }
        when (habit) {
            "cube" -> {
                val k = 0.866f
                val top = Offset(cx, cy - a)
                val ur = Offset(cx + k * a, cy - a / 2f)
                val lr = Offset(cx + k * a, cy + a / 2f)
                val bot = Offset(cx, cy + a)
                val ll = Offset(cx - k * a, cy + a / 2f)
                val ul = Offset(cx - k * a, cy - a / 2f)
                val c = Offset(cx, cy)
                poly(light, listOf(top, ur, c, ul))
                poly(base, listOf(ul, c, bot, ll))
                poly(dark, listOf(c, ur, lr, bot))
            }
            "octa" -> {
                val t = Offset(cx, cy - a * 1.25f)
                val b = Offset(cx, cy + a * 1.25f)
                val l = Offset(cx - a * 1.05f, cy + a * 0.05f)
                val r = Offset(cx + a * 1.05f, cy + a * 0.05f)
                val f = Offset(cx + a * 0.15f, cy + a * 0.3f)
                poly(base, listOf(t, l, f))
                poly(light, listOf(t, r, f))
                poly(dark, listOf(l, b, f))
                poly(base, listOf(r, b, f))
            }
            "hex" -> column(cx, cy - a * 0.3f, cy + a * 1.05f, a * 0.55f, a * 0.7f)
            "prism" -> {
                column(cx - a * 0.35f, cy - a * 0.2f, cy + a * 1.0f, a * 0.4f, a * 0.35f)
                column(cx + a * 0.4f, cy - a * 0.5f, cy + a * 1.0f, a * 0.35f, a * 0.5f)
            }
            "cluster" -> {
                column(cx - a * 0.75f, cy, cy + a * 1.0f, a * 0.3f, a * 0.5f)
                column(cx + a * 0.75f, cy + a * 0.1f, cy + a * 1.0f, a * 0.3f, a * 0.45f)
                column(cx, cy - a * 0.5f, cy + a * 1.05f, a * 0.38f, a * 0.6f)
            }
            "layered" -> {
                for (i in 0 until 4) {
                    val y = cy + a * 0.7f - i * a * 0.42f
                    val sh = a * 0.35f
                    poly(if (i % 2 == 0) base else light, listOf(Offset(cx - a * 1.1f + sh, y - a * 0.25f), Offset(cx + a * 1.1f + sh, y - a * 0.25f), Offset(cx + a * 1.1f, y + a * 0.12f), Offset(cx - a * 1.1f, y + a * 0.12f)))
                    poly(dark, listOf(Offset(cx - a * 1.1f, y + a * 0.12f), Offset(cx + a * 1.1f, y + a * 0.12f), Offset(cx + a * 1.1f, y + a * 0.27f), Offset(cx - a * 1.1f, y + a * 0.27f)))
                }
            }
            "botryoidal" -> {
                val spots = listOf(
                    Triple(-0.6f, 0.2f, 0.7f), Triple(0.5f, 0.25f, 0.75f), Triple(0f, -0.35f, 0.8f),
                    Triple(-0.15f, 0.55f, 0.5f), Triple(0.85f, -0.2f, 0.45f), Triple(-1.0f, -0.25f, 0.45f)
                )
                for ((dx, dy, rr) in spots) {
                    val c = Offset(cx + dx * a, cy + dy * a)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(light, base, dark),
                            center = Offset(c.x - rr * a * 0.3f, c.y - rr * a * 0.3f), radius = rr * a * 1.2f
                        ),
                        radius = rr * a, center = c
                    )
                    drawCircle(color = edge, radius = rr * a, center = c, style = Stroke(width = 2f))
                }
            }
            "banded" -> {
                for (i in 0 until 7) {
                    val f = 1f - i / 7f
                    val col = if (i % 2 == 0) lerp(base, Color.White, 0.1f + 0.5f * (i / 7f)) else lerp(base, Color.Black, 0.25f)
                    drawOval(color = col, topLeft = Offset(cx - a * 1.35f * f, cy - a * 0.95f * f), size = Size(a * 2.7f * f, a * 1.9f * f))
                }
                drawOval(color = edge, topLeft = Offset(cx - a * 1.35f, cy - a * 0.95f), size = Size(a * 2.7f, a * 1.9f), style = Stroke(width = 2f))
            }
            else -> {
                val rs = floatArrayOf(1.05f, 0.9f, 1.1f, 0.85f, 1.0f, 0.8f, 1.08f, 0.92f, 1.0f)
                val path = Path()
                for (i in rs.indices) {
                    val ang = (i.toFloat() / rs.size) * 6.2831853f
                    val px = cx + kotlin.math.cos(ang) * a * 1.25f * rs[i]
                    val py = cy + kotlin.math.sin(ang) * a * 0.95f * rs[i]
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                drawPath(path, Brush.linearGradient(colors = listOf(light, base, dark), start = Offset(cx - a, cy - a), end = Offset(cx + a, cy + a)))
                drawPath(path, edge, style = Stroke(width = 2f))
                for (i in 0 until 7) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.35f), radius = a * 0.05f * (1 + i % 3),
                        center = Offset(cx + (i * 37 % 90 - 45) / 45f * a, cy + (i * 53 % 70 - 35) / 35f * a * 0.6f)
                    )
                }
            }
        }
    }
}
