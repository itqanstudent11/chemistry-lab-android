package org.arabicchemistry.lab

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private fun number(x: Double): String = DecimalFormat(if (x != 0.0 && (kotlin.math.abs(x) < 0.00001 || kotlin.math.abs(x) >= 1e9)) "0.######E0" else "0.######", DecimalFormatSymbols(Locale.US)).format(x)

@Composable internal fun LabScreen() {
    val s = LocalWords.current
    var mode by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf(s.text("حفظ الذرات", "Atom conservation"), s.text("الذوبان", "Dissolution"), s.text("التخفيف", "Dilution"), s.text("الغاز المثالي", "Ideal gas"), s.text("الحموضة", "Acidity"))
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Heading(s.text("المختبر الافتراضي الآمن", "Safe virtual laboratory")) }
        item { Text(s.text("نماذج رقمية تعليمية؛ ليست تعليمات لإجراء تجارب أو خلط مواد.", "Digital educational models; no instructions for performing experiments or mixing substances.")) }
        item { Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            titles.forEachIndexed { i, title -> FilterChip(selected = mode == i, onClick = { mode = i }, label = { Text(title) }) }
        } }
        item { when(mode) { 0 -> ParticleModel(false); 1 -> ParticleModel(true); 2 -> ModelCalculator("dilution"); 3 -> ModelCalculator("gas"); 4 -> ModelCalculator("ph") } }
    }
}

@Composable private fun ParticleModel(dissolution: Boolean) {
    val s = LocalWords.current
    var a by rememberSaveable(dissolution) { mutableIntStateOf(if (dissolution) 8 else 4) }
    var b by rememberSaveable(dissolution) { mutableIntStateOf(2) }
    val result = ChemistryMath.water(a, b)
    val summary = if (dissolution) s.text("$a جسيم سكر موزعة في $b وحدات حجم نسبية؛ التركيز النسبي ${number(a.toDouble() / b)}.",
        "$a sugar particles distributed in $b relative volume units; relative concentration ${number(a.toDouble() / b)}.")
        else s.text("الماء: ${result.water} جزيئات. المتبقي H₂: ${result.hydrogenLeft}، O₂: ${result.oxygenLeft}. ذرات الهيدروجين قبل وبعد: ${2*a}؛ الأكسجين: ${2*b}.",
            "Water: ${result.water} molecules. Remaining H₂: ${result.hydrogenLeft}; O₂: ${result.oxygenLeft}. H atoms before and after: ${2*a}; O atoms: ${2*b}.")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoCard(if (dissolution) s.text("نموذج جسيمي للذوبان", "Particle model of dissolution") else s.text("2H₂ + O₂ → 2H₂O", "2H₂ + O₂ → 2H₂O"),
            if (dissolution) s.text("جزيئات السكر تتوزع في الماء وتحتفظ بهويتها. الحركة رمزية؛ لا يمثل النموذج السرعة أو الحرارة أو حد الذوبانية.",
                "Sugar molecules disperse in water and retain their identity. Motion is symbolic; rate, temperature and solubility limits are not modelled.")
            else s.text("يحفظ النموذج عدد الذرات ويحسب المتفاعل المحدد بجزيئات صحيحة فقط. لا يمثل سرعة التفاعل أو طاقته أو تنفيذه؛ الهيدروجين والأكسجين خليط خطر عمليًا.",
                "The model conserves atoms and uses whole molecules to identify the limiting reactant. Rate, energy and practical execution are not modelled; hydrogen/oxygen mixtures are hazardous."))
        val firstLabel = if (dissolution) s.text("جسيمات السكر: $a", "Sugar particles: $a") else s.text("جزيئات H₂: $a", "H₂ molecules: $a")
        Text(firstLabel)
        Slider(value = a.toFloat(), onValueChange = { a = it.toInt() }, valueRange = 0f..20f, steps = 19, modifier = Modifier.semantics { contentDescription = firstLabel })
        val secondLabel = if (dissolution) s.text("حجم الماء النسبي: $b", "Relative water volume: $b") else s.text("جزيئات O₂: $b", "O₂ molecules: $b")
        Text(secondLabel)
        Slider(value = b.toFloat(), onValueChange = { b = it.toInt() }, valueRange = if (dissolution) 1f..10f else 0f..10f,
            steps = if (dissolution) 8 else 9, modifier = Modifier.semantics { contentDescription = secondLabel })
        val transition = rememberInfiniteTransition(label = "particle motion")
        val phase by transition.animateFloat(0f, 6.283185f, infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart), label = "phase")
        val count = if (dissolution) a else result.water
        Canvas(Modifier.fillMaxWidth().height(220.dp).background(Color(0xFFE2F0FA)).semantics { contentDescription = summary }) {
            repeat(count) { i ->
                val x = size.width * ((i % 5 + 1) / 6f) + sin(phase + i) * 7.dp.toPx()
                val y = size.height * ((i / 5 + 1) / 5f) + cos(phase + i) * 5.dp.toPx()
                drawCircle(Blue, 9.dp.toPx(), Offset(x, y))
                if (!dissolution) {
                    drawCircle(Color(0xFFD27B20), 5.dp.toPx(), Offset(x - 10.dp.toPx(), y + 8.dp.toPx()))
                    drawCircle(Color(0xFFD27B20), 5.dp.toPx(), Offset(x + 10.dp.toPx(), y + 8.dp.toPx()))
                }
            }
        }
        InfoCard(s.text("نتيجة النموذج", "Model result"), summary)
    }
}

@Composable internal fun GoldCalculator() {
    val s = LocalWords.current
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) { Text(s.text("حاسبة محتوى الذهب", "Contained-gold calculator")) }
        if (expanded) ModelCalculator("gold")
    }
}
@Composable private fun ModelCalculator(mode: String) {
    val s = LocalWords.current
    var a by rememberSaveable(mode) { mutableStateOf("") }
    var b by rememberSaveable(mode) { mutableStateOf("") }
    var c by rememberSaveable(mode) { mutableStateOf("") }
    val labels = when(mode) {
        "gold" -> listOf(s.text("كتلة الخام (طن متري)", "Ore mass (metric tonnes)"), s.text("العيار (g/t)", "Grade (g/t)"), s.text("الاسترداد الافتراضي (%)", "Hypothetical recovery (%)"))
        "gas" -> listOf(s.text("كمية المادة (mol)", "Amount (mol)"), s.text("درجة الحرارة المطلقة (K)", "Absolute temperature (K)"), s.text("الحجم (L)", "Volume (L)"))
        "ph" -> listOf(s.text("تركيز الحمض (mol/L)", "Acid concentration (mol/L)"))
        else -> listOf(s.text("التركيز الأول C₁ (mol/L)", "Initial concentration C₁ (mol/L)"), s.text("الحجم الأول V₁ (mL)", "Initial volume V₁ (mL)"), s.text("الحجم النهائي V₂ (mL)", "Final volume V₂ (mL)"))
    }
    val explanation = when(mode) {
        "gold" -> s.text("المحتوى (g) = الكتلة (t) × العيار (g/t). الكتلة والعيار غير سالبين، والاسترداد من 0 إلى 100%. ليست النتيجة احتياطيًا أو جدوى اقتصادية أو وصفة استخلاص.",
            "Contained gold (g) = mass (t) × grade (g/t). Mass and grade must be nonnegative; recovery is 0–100%. This is not a reserve, economic assessment or extraction procedure.")
        "gas" -> s.text("P = nRT/V، حيث R = 8.314462618 L·kPa·mol⁻¹·K⁻¹. n ≥ 0 وT وV موجبان. يهمل حجم الجزيئات والتجاذب؛ تقل دقته قرب التكاثف وعند الضغط العالي.",
            "P = nRT/V, R = 8.314462618 L·kPa·mol⁻¹·K⁻¹. n ≥ 0; T and V must be positive. Molecular volume and attractions are neglected; accuracy falls near condensation and at high pressure.")
        "ph" -> s.text("حمض قوي أحادي البروتون مثالي عند 25°C فقط، 0 ≤ C ≤ 0.1 mol/L. [H⁺] = (C + √(C² + 4Kw))/2؛ Kw = 10⁻¹⁴. يشمل تأين الماء؛ يهمل النشاط الأيوني ولا يصلح للأحماض الضعيفة أو الخلائط.",
            "Ideal strong monoprotic acid at 25°C only, 0 ≤ C ≤ 0.1 mol/L. [H⁺] = (C + √(C² + 4Kw))/2, Kw = 10⁻¹⁴. Includes water autoionization; ignores ionic activity and does not apply to weak acids or mixtures.")
        else -> s.text("C₂ = C₁V₁/V₂. التركيز غير سالب وV₁ موجب وV₂ ≥ V₁. حفظ المذاب دون تفاعل أو فقد؛ جميع الأحجام بالمليلتر. هذه علاقة حسابية وليست تعليمات لتحضير محلول.",
            "C₂ = C₁V₁/V₂. Concentration must be nonnegative, V₁ positive and V₂ ≥ V₁. Solute is conserved without reaction or loss; both volumes are in mL. This is a calculation, not solution-preparation instructions.")
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoCard(s.text("المعادلة وحدود النموذج", "Equation and model limits"), explanation)
        val values = listOf(a, b, c)
        labels.forEachIndexed { i, label ->
            OutlinedTextField(value = values[i], onValueChange = { when(i) { 0 -> a = it; 1 -> b = it; else -> c = it } },
                label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
        }
        Text(s.text("يمكن استخدام الأرقام العربية أو الإنجليزية والفاصلة العشرية؛ دون فواصل آلاف.", "Arabic or English digits and decimal marks are accepted; do not use thousands separators."), style = MaterialTheme.typography.bodySmall)
        val used = values.take(labels.size)
        val parsed = used.map { ChemistryMath.parseNumber(it) }
        val output = if (parsed.all { it != null }) runCatching {
            val x = parsed[0]!!
            when(mode) {
                "gold" -> ChemistryMath.gold(x, parsed[1]!!, parsed[2]!!).let { s.text("المحتوى النظري: ${number(it.containedGrams)} g\nالمسترد افتراضيًا: ${number(it.recoveredGrams)} g", "Theoretical contained gold: ${number(it.containedGrams)} g\nHypothetically recovered: ${number(it.recoveredGrams)} g") }
                "gas" -> "P = ${number(ChemistryMath.gasPressureKpa(x, parsed[1]!!, parsed[2]!!))} kPa"
                "ph" -> "pH = ${number(ChemistryMath.acidPh(x))}"
                else -> "C₂ = ${number(ChemistryMath.dilution(x, parsed[1]!!, parsed[2]!!))} mol/L"
            }
        }.getOrNull() else null
        if (output != null) InfoCard(s.text("النتيجة", "Result"), output)
        else Text(if (used.all { it.isBlank() }) s.text("أدخل القيم لعرض النتيجة.", "Enter values to see the result.") else s.text("قيم غير صالحة. راجع الحدود والوحدات المذكورة أعلاه.", "Invalid values. Check the limits and units above."), color = MaterialTheme.colorScheme.error)
    }
}
