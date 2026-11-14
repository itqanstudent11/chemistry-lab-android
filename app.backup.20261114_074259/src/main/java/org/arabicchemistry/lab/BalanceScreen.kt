package org.arabicchemistry.lab

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable internal fun BalanceScreen(known: Set<String>) {
    val s = LocalWords.current
    var input by rememberSaveable { mutableStateOf("H2 + O2 -> H2O") }
    var output by rememberSaveable { mutableStateOf("") }
    var invalid by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Heading(s.text("موازنة المعادلات", "Equation balancing"))
        InfoCard(s.text("حفظ الذرات", "Atom conservation"), s.text(
            "أدخل الصيغ المتعادلة دون معاملات، وافصل طرفي المعادلة بـ -> أو = أو →. تقبل الأقواس والأرقام السفلية. يتحقق الحساب من حفظ الذرات، ولا يثبت إمكانية حدوث التفاعل أو شروطه ولا يوازن الشحنات الأيونية. لا تجرب المعادلات عمليًا اعتمادًا على هذه الأداة.",
            "Enter neutral formulas without coefficients; separate sides with ->, = or →. Parentheses and subscript digits are supported. The calculation checks atom conservation, not reaction feasibility or conditions, and does not balance ionic charges. Do not perform experiments based on this tool."))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("H2 + O2 -> H2O", "Fe + O2 -> Fe2O3", "CaCO3 -> CaO + CO2").forEach { sample ->
                OutlinedButton(onClick = { input = sample; output = ""; invalid = false }) { Text(sample) }
            }
        }
        OutlinedTextField(input, { input = it.take(251); output = ""; invalid = false }, label = { Text(s.text("معادلة بصيغ متعادلة", "Neutral equation")) }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val result = runCatching { EquationBalancer.balance(input, known) }.getOrNull()
            invalid = result == null; output = result?.formatted().orEmpty()
        }) { Text(s.text("وازن المعادلة", "Balance equation")) }
        if (output.isNotBlank()) InfoCard(s.text("أصغر معاملات صحيحة موجبة", "Smallest positive integer coefficients"), output)
        if (invalid) Text(s.text("تعذر الحل: راجع الصيغ والعناصر. يجب أن يكون الحل موجبًا ووحيد النسبة. الحد 250 حرفًا و12 مادة؛ الأيونات والهيدرات النقطية غير مدعومة.",
            "Cannot solve: check formulas and elements. A positive solution with unique ratios is required. Limits: 250 characters, 12 species; ions and dot hydrates are unsupported."), color = MaterialTheme.colorScheme.error)
        SourceLink(s.text("مرجع الموازنة • OpenStax", "Balancing reference • OpenStax"), "https://openstax.org/books/chemistry-2e/pages/4-1-writing-and-balancing-chemical-equations")
    }
}
