package org.arabicchemistry.lab

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable internal fun Heading(title: String) { Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
@Composable internal fun InfoCard(title: String, body: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text(body, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
@Composable internal fun NavCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@Composable internal fun SourceLink(title: String, url: String) {
    val context = LocalContext.current
    val s = LocalWords.current
    TextButton(onClick = {
        val uri = Uri.parse(url)
        if (uri.scheme == "https" && !uri.host.isNullOrBlank()) {
            try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
            catch (_: ActivityNotFoundException) { Toast.makeText(context, s.text("لا يوجد متصفح لفتح المرجع", "No browser available to open this reference"), Toast.LENGTH_SHORT).show() }
        }
    }, modifier = Modifier.fillMaxWidth()) { Text(title) }
}

@Composable internal fun CurriculumScreen(lessons: List<Lesson>, done: Set<String>, favorites: Set<String>, savedOnly: Boolean, onLesson: (String) -> Unit) {
    val s = LocalWords.current
    var query by rememberSaveable(savedOnly) { mutableStateOf("") }
    var level by rememberSaveable { mutableIntStateOf(0) }
    val levels = if (s.english) listOf("All", "Foundation", "Intermediate", "University", "Specialist") else listOf("الكل", "تأسيسي", "متوسط", "جامعي", "تخصصي")
    val visible = lessons.filter { l -> (!savedOnly || l.id in favorites) && (level == 0 || l.level == levels[level]) &&
        (query.isBlank() || listOf(l.title, l.section, l.intro, l.detail).any { it.contains(query.trim(), ignoreCase = true) }) }
    LazyColumn(Modifier.fillMaxSize().testTag("lesson-list"), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Heading(if (savedOnly) s.text("المفضلة", "Saved lessons") else s.text("فهرس المنهج", "Curriculum index")) }
        item { OutlinedTextField(query, { query = it }, label = { Text(s.text("ابحث في المنهج", "Search the curriculum")) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            levels.forEachIndexed { i, name -> FilterChip(selected = level == i, onClick = { level = i }, label = { Text(name) }) }
        } }
        item { Text(s.text("${visible.size} درسًا • تكتمل الدروس عند إجابة جميع أسئلتها", "${visible.size} lessons • complete every question to finish a lesson")) }
        if (visible.isEmpty()) item { InfoCard(s.text("لا توجد نتائج", "No results"), s.text("عدّل البحث أو المستوى، أو أضف درسًا إلى المفضلة.", "Change the search or level, or save a lesson first.")) }
        visible.groupBy { it.section }.forEach { (section, entries) ->
            item(key = "section-$section") { Text(section, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
            items(entries, key = { it.id }) { lesson ->
                NavCard(lesson.title, "${lesson.level} • ${if (lesson.id in done) s.text("مكتمل ✓", "Completed ✓") else s.text("ابدأ القراءة", "Read lesson")}") { onLesson(lesson.id) }
            }
        }
    }
}

@Composable internal fun LessonScreen(lesson: Lesson, lessons: List<Lesson>, favorite: Boolean, completed: Boolean,
    store: LearningStore, onFavorite: () -> Unit, onComplete: () -> Unit, onLesson: (String) -> Unit) {
    val s = LocalWords.current
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var passed by remember(lesson.id) { mutableStateOf(store.passed(lesson.id).filter { it in lesson.questions.indices }.toSet()) }
    val index = lessons.indexOfFirst { it.id == lesson.id }
    LazyColumn(state = state, modifier = Modifier.fillMaxSize().testTag("lesson-content"), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item(key = "header-${lesson.id}") {
            Text("${lesson.level} • ${lesson.section}", color = MaterialTheme.colorScheme.primary)
            Heading(lesson.title)
            TextButton(onClick = onFavorite) { Text(if (favorite) s.text("إزالة من المفضلة", "Remove from saved") else s.text("إضافة إلى المفضلة", "Save lesson")) }
            InfoCard(s.text("أهداف التعلم", "Learning objectives"), lesson.objectives.joinToString("\n") { "• $it" })
        }
        item {
            Text(s.text("فهرس الدرس", "Lesson contents"), style = MaterialTheme.typography.titleLarge)
            listOf(s.text("مقدمة ومفاهيم", "Introduction"), s.text("التفسير العلمي", "Explanation"), s.text("مثال محلول", "Worked example"),
                s.text("الخلاصة", "Summary"), s.text("المتطلبات والمراجع", "Prerequisites and references"), s.text("اختبار الدرس", "Lesson quiz")).forEachIndexed { i, label ->
                TextButton(onClick = { scope.launch { state.animateScrollToItem(i + 2) } }) { Text(label) }
            }
        }
        item { InfoCard(s.text("مقدمة ومفاهيم", "Introduction"), lesson.intro) }
        item { InfoCard(s.text("التفسير العلمي", "Explanation"), lesson.detail) }
        item { InfoCard(s.text("مثال محلول", "Worked example"), lesson.example) }
        item { InfoCard(s.text("الخلاصة", "Summary"), lesson.summary) }
        item {
            Text(s.text("المتطلبات السابقة", "Prerequisites"), style = MaterialTheme.typography.titleLarge)
            if (lesson.prerequisites.isEmpty()) Text(s.text("لا توجد متطلبات سابقة.", "No prerequisites."))
            lesson.prerequisites.forEach { id -> lessons.find { it.id == id }?.let { l -> TextButton(onClick = { onLesson(id) }) { Text(l.title) } } }
            Text(s.text("مراجع للمراجعة", "Further reading"), style = MaterialTheme.typography.titleLarge)
            Text(lesson.source)
            lesson.sourceUrls.forEachIndexed { i, url -> SourceLink(s.text("فتح المرجع ${i + 1}", "Open reference ${i + 1}") + " • " + Uri.parse(url).host.orEmpty(), url) }
        }
        item {
            Heading(s.text("اختبار الدرس", "Lesson quiz"))
            Text(s.text("${passed.size} / ${lesson.questions.size} إجابات صحيحة", "${passed.size} / ${lesson.questions.size} questions passed"))
            if (completed) Text(if (passed.size == lesson.questions.size) s.text("الدرس مكتمل ✓", "Lesson completed ✓") else s.text("إنجاز محفوظ من الإصدار السابق؛ يمكنك إكمال أسئلة المنهج الموسع.", "Completion retained from the previous version; you can also complete the expanded quiz."), color = MaterialTheme.colorScheme.primary)
        }
        lesson.questions.forEachIndexed { qi, question ->
            item(key = "${lesson.id}-q-$qi") {
                var choice by rememberSaveable(lesson.id, qi, s.english) { mutableIntStateOf(-1) }
                val alreadyPassed = qi in passed
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${qi + 1}. ${question.question}", style = MaterialTheme.typography.titleMedium)
                        question.options.forEachIndexed { oi, option ->
                            OutlinedButton(onClick = {
                                choice = oi
                                if (oi == question.answer) {
                                    passed = passed + qi
                                    store.savePassed(lesson.id, passed)
                                    if (passed.size == lesson.questions.size) onComplete()
                                }
                            }, enabled = !alreadyPassed, modifier = Modifier.fillMaxWidth().testTag("quiz-${lesson.id}-$qi-$oi")) {
                                Text(if (alreadyPassed && oi == question.answer) "✓ $option" else option)
                            }
                        }
                        if (choice >= 0 || alreadyPassed) {
                            Text(if (alreadyPassed) s.text("إجابة صحيحة", "Correct") else s.text("راجع التفسير ثم حاول مجددًا", "Review the explanation and try again"), color = MaterialTheme.colorScheme.primary)
                            Text(question.explanation)
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { onLesson(lessons[index - 1].id) }, enabled = index > 0) { Text(s.text("السابق", "Previous")) }
                TextButton(onClick = { onLesson(lessons[index + 1].id) }, enabled = index < lessons.lastIndex) { Text(s.text("التالي", "Next")) }
            }
        }
    }
}

internal fun Words.sectionName(section: String) = when(section) {
    "gold" -> text("الذهب والمعادن", "Gold and minerals")
    "safety" -> text("السلامة المهنية", "Occupational safety")
    else -> text("جيولوجيا اليمن", "Geology of Yemen")
}
@Composable internal fun GuidesScreen(guides: List<Guide>, section: String, onGuide: (String) -> Unit) {
    val s = LocalWords.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Heading(s.sectionName(section)) }
        if (section == "gold") item { GoldCalculator() }
        items(guides, key = { it.id }) { guide -> NavCard(guide.title, guide.intro) { onGuide(guide.id) } }
    }
}
@Composable internal fun GuideScreen(guide: Guide) {
    val s = LocalWords.current
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LazyColumn(state = state, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Heading(guide.title); Text(guide.intro); InfoCard(s.text("حالة التوثيق", "Evidence status"), guide.reviewStatus) }
        item { Text(s.text("الفهرس", "Contents"), style = MaterialTheme.typography.titleLarge)
            guide.topics.forEachIndexed { i, topic -> TextButton(onClick = { scope.launch { state.animateScrollToItem(i + 2) } }) { Text(topic.title) } }
        }
        items(guide.topics, key = { it.id }) { InfoCard(it.title, it.body) }
        item { Heading(s.text("المراجع", "References")); guide.sources.forEach { SourceLink(it.title, it.url) } }
    }
}
