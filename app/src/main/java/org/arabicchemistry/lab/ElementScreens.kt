package org.arabicchemistry.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private fun Words.category(key: String): String = when(key) {
    "alkali" -> text("فلزات قلوية", "Alkali metals")
    "alkaline" -> text("قلوية ترابية", "Alkaline earth metals")
    "transition" -> text("فلزات انتقالية", "Transition metals")
    "post-transition" -> text("فلزات بعد انتقالية", "Post-transition metals")
    "metalloid" -> text("أشباه فلزات", "Metalloids")
    "nonmetal" -> text("لافلزات", "Nonmetals")
    "halogen" -> text("هالوجينات", "Halogens")
    "noble" -> text("غازات نبيلة", "Noble gases")
    "lanthanide" -> text("لانثانيدات", "Lanthanides")
    "actinide" -> text("أكتينيدات", "Actinides")
    else -> key
}
private fun categoryColor(key: String) = when(key) {
    "alkali" -> Color(0xFFFFDED6); "alkaline" -> Color(0xFFFFEDCA)
    "transition" -> Color(0xFFD5E9FC); "post-transition" -> Color(0xFFD9EEE4)
    "metalloid" -> Color(0xFFE2EBC9); "nonmetal" -> Color(0xFFCEEDE8)
    "halogen" -> Color(0xFFE3DDF5); "noble" -> Color(0xFFEEDDF2)
    "lanthanide" -> Color(0xFFF5E6D0); else -> Color(0xFFE3DFFC)
}
@Composable internal fun ElementsScreen(elements: List<Element>, onElement: (Int) -> Unit) {
    val s = LocalWords.current
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var grid by rememberSaveable { mutableStateOf(true) }
    val q = query.trim()
    val number = ChemistryMath.parseNumber(q)?.takeIf { it % 1.0 == 0.0 }?.toInt()
    val filtered = elements.filter { e -> (category.isEmpty() || e.category == category) &&
        (q.isEmpty() || e.name.contains(q, true) || e.nameEn.contains(q, true) || e.symbol.contains(q, true) || e.number == number) }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Heading(s.text("الجدول الدوري • 118 عنصرًا", "Periodic table • 118 elements"))
        OutlinedTextField(query, { query = it }, label = { Text(s.text("الاسم أو الرمز أو العدد الذري", "Name, symbol or atomic number")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(category.isEmpty(), { category = "" }, label = { Text(s.text("الكل", "All")) })
            elements.map { it.category }.distinct().forEach { key -> FilterChip(category == key, { category = key }, label = { Text(s.category(key)) }) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(s.text("${filtered.size} نتيجة", "${filtered.size} results"))
            TextButton(onClick = { grid = !grid }) { Text(if (grid) s.text("عرض القائمة", "List view") else s.text("عرض الشبكة", "Grid view")) }
        }
        if (filtered.isEmpty()) Text(s.text("لا توجد عناصر مطابقة.", "No matching elements."))
        if (grid && q.isEmpty() && category.isEmpty()) {
            LazyColumn(Modifier.weight(1f)) {
                item { Text(s.text("اسحب أفقيًا لرؤية المجموعات 1–18. السلسلتان أسفل الجدول.", "Scroll horizontally to view groups 1–18. The two series appear below.")) }
                item { PeriodicGrid(elements, onElement) }
            }
        } else LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered, key = { it.number }) { e -> NavCard("${e.number} • ${e.symbol} • ${if (s.english) e.nameEn else e.name}", s.category(e.category)) { onElement(e.number) } }
        }
    }
}
@Composable private fun PeriodicGrid(elements: List<Element>, onElement: (Int) -> Unit) {
    val s = LocalWords.current
    // International group order is always left to right, including in the Arabic interface.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.width(28.dp))
                (1..18).forEach { Box(Modifier.width(62.dp), contentAlignment = Alignment.Center) { Text("$it") } }
            }
            (1..7).forEach { period ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("$period", Modifier.width(28.dp))
                    (1..18).forEach { group ->
                        val e = elements.find { it.period == period && it.group == group && it.number !in 57..71 && it.number !in 89..103 }
                        when {
                            e != null -> ElementCell(e, onElement)
                            group == 3 && period in 6..7 -> Box(Modifier.size(62.dp, 74.dp).background(Color(0xFFE7EAF0)), contentAlignment = Alignment.Center) { Text(if (period == 6) "57–71\n↓" else "89–103\n↓") }
                            else -> Spacer(Modifier.size(62.dp, 74.dp))
                        }
                    }
                }
            }
            listOf(57..71, 89..103).forEach { range ->
                Text(s.category(if (range.first == 57) "lanthanide" else "actinide"), Modifier.padding(top = 12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Spacer(Modifier.width(160.dp))
                    elements.filter { it.number in range }.forEach { ElementCell(it, onElement) }
                }
            }
        }
    }
}
@Composable private fun ElementCell(e: Element, onElement: (Int) -> Unit) {
    val s = LocalWords.current
    val name = if (s.english) e.nameEn else e.name
    Card(onClick = { onElement(e.number) }, modifier = Modifier.size(62.dp, 74.dp).semantics { contentDescription = "${e.number} $name ${e.symbol}" },
        colors = CardDefaults.cardColors(containerColor = categoryColor(e.category))) {
        Column(Modifier.fillMaxSize().padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("${e.number}", style = MaterialTheme.typography.labelSmall)
            Text(e.symbol, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable internal fun ElementScreen(e: Element) {
    val s = LocalWords.current
    val groupLabel = when (e.category) {
        "lanthanide" -> s.text("السلسلة: لانثانيدات (منفصلة)", "Series: lanthanides (detached)")
        "actinide" -> s.text("السلسلة: أكتينيدات (منفصلة)", "Series: actinides (detached)")
        else -> s.text("المجموعة: ${e.group}", "Group: ${e.group}")
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Heading("${e.symbol} • ${if (s.english) e.nameEn else e.name}") }
        item { InfoCard(s.text("الخصائص الأساسية", "Basic properties"), s.text(
            "العدد الذري: ${e.number}\nالدورة: ${e.period}\n$groupLabel\nالفئة: ${s.category(e.category)}\nالفصيلة الإلكترونية: ${e.block}",
            "Atomic number: ${e.number}\nPeriod: ${e.period}\n$groupLabel\nCategory: ${s.category(e.category)}\nBlock: ${e.block}")) }
        item { InfoCard(if (e.massKind == "isotope") s.text("العدد الكتلي لنظير مختار", "Mass number of a selected isotope") else s.text("الوزن الذري النسبي المختصر", "Abridged relative atomic weight"),
            e.mass + (if (e.massUncertainty.isNotBlank()) " ± ${e.massUncertainty}" else "") + "\n" +
                if (e.massKind == "isotope") s.text("الأقواس تعني عددًا كتليًا، وليس وزنًا ذريًا معياريًا أو وفرة طبيعية.", "Brackets indicate an isotope mass number, not a standard atomic weight or natural abundance.")
                else s.text("قيمة نسبية بلا وحدة؛ قد يختلف تركيب النظائر طبيعيًا. البيانات المختصرة وفق CIAAW 2024.", "Dimensionless relative value; isotopic composition can vary naturally. Abridged CIAAW 2024 values.")) }
        item { InfoCard(s.text("الحالة عند 20°C ونحو 1 atm والملخص", "State at 20°C and ~1 atm; overview"), (if (s.english) e.stateEn else e.stateAr) + "\n\n" + if (s.english) e.descriptionEn else e.descriptionAr) }
        if (e.number in 57..71 || e.number in 89..103) item { Text(s.text("عرضت المجموعة 3 كموضع تعليمي للسلسلة، وليس حسمًا للنقاش حول تعريف عناصر المجموعة 3.", "Group 3 is a layout convention for this series, not a resolution of group-3 membership.")) }
        item { InfoCard(s.text("التواجد في اليمن", "Occurrence in Yemen"), s.text("لا تتوافر في هذه الحزمة بيانات محلية معتمدة لكل عنصر. وجود معدن أو عنصر في تقرير إقليمي لا يثبت صلاحية موقع للتعدين.", "This package does not provide certified local occurrence data for every element. Regional reports do not establish that a site is suitable for mining.")) }
        item { SourceLink(s.text("مرجع العنصر", "Element reference"), e.sourceUrl); SourceLink(s.text("مرجع الوزن الذري", "Atomic-weight reference"), e.massSourceUrl) }
    }
}
