package org.arabicchemistry.lab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable internal fun GlossaryScreen(entries: List<GlossaryEntry>, lessons: List<Lesson>, onLesson: (String) -> Unit) {
    val s = LocalWords.current
    var query by rememberSaveable { mutableStateOf("") }
    val visible = entries.filter { e -> query.isBlank() || listOf(e.ar, e.en, e.definitionAr, e.definitionEn).any { it.contains(query.trim(), true) } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Heading(s.text("معجم المصطلحات", "Chemistry glossary")) }
        item { OutlinedTextField(query, { query = it }, label = { Text(s.text("ابحث عن مصطلح باللغتين", "Search terms in either language")) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { Text(s.text("${visible.size} مصطلحًا", "${visible.size} terms")) }
        items(visible, key = { it.id }) { e ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                InfoCard(s.text(e.ar, e.en), s.text(e.definitionAr, e.definitionEn))
                lessons.find { it.id == e.lessonId }?.let { lesson ->
                    TextButton(onClick = { onLesson(lesson.id) }) { Text(s.text("الدرس والمرجع: ", "Lesson and reference: ") + lesson.title) }
                }
            }
        }
    }
}
