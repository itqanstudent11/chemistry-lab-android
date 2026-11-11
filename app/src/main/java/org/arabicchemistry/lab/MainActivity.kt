package org.arabicchemistry.lab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class Words(val english: Boolean) {
    fun text(ar: String, en: String) = if (english) en else ar
}
internal val LocalWords = staticCompositionLocalOf { Words(false) }
internal val Blue = Color(0xFF08649A)
internal val Ink = Color(0xFF153249)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val loaded by produceState<Result<Library>?>(null) {
                value = withContext(Dispatchers.IO) { runCatching { ContentRepository.load(applicationContext) } }
            }
            MaterialTheme(colorScheme = lightColorScheme(primary = Blue, secondary = Color(0xFF805800),
                background = Color(0xFFF1F6FA), surface = Color.White, onSurface = Ink, onBackground = Ink)) {
                when {
                    loaded == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    loaded!!.isFailure -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("تعذر تحميل المحتوى المحلي. أعد تثبيت الحزمة الكاملة.\nLocal content could not be loaded. Reinstall the complete package.")
                    }
                    else -> ChemistryApp(loaded!!.getOrThrow())
                }
            }
        }
    }
}

@Composable internal fun ChemistryApp(library: Library) {
    val context = LocalContext.current
    val store = remember { LearningStore(context, library.lessonsAr.map { it.id }) }
    var english by remember { mutableStateOf(store.english()) }
    var done by remember { mutableStateOf(store.completed()) }
    var favorites by remember { mutableStateOf(store.favorites()) }
    var page by rememberSaveable { mutableStateOf("home") }
    var lessonReturn by rememberSaveable { mutableStateOf("lessons") }
    var lessonId by rememberSaveable { mutableStateOf(library.lessonsAr.first().id) }
    var elementNumber by rememberSaveable { mutableIntStateOf(1) }
    var guideId by rememberSaveable { mutableStateOf("") }
    var guideSection by rememberSaveable { mutableStateOf("geology") }
    val s = Words(english)
    val lessons = library.lessons(english)
    val guides = library.guides(english)
    fun openLesson(id: String) { if (page != "lesson") lessonReturn = if (page == "favorites") "favorites" else "lessons"; lessonId = id; page = "lesson" }
    fun openGuideSection(section: String) { guideSection = section; page = "guides" }
    fun goBack() { page = when (page) { "lesson" -> lessonReturn; "element" -> "elements"; "guide" -> "guides"; else -> "home" } }
    BackHandler(page != "home") { goBack() }
    CompositionLocalProvider(LocalWords provides s, LocalLayoutDirection provides if (english) LayoutDirection.Ltr else LayoutDirection.Rtl) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).windowInsetsPadding(WindowInsets.safeDrawing)) {
            Surface(shadowElevation = 1.dp) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (page != "home") TextButton(onClick = { goBack() }) { Text(s.text("رجوع", "Back")) }
                    Text(s.text("مختبر الكيمياء", "Chemistry Lab"), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    TextButton(onClick = { page = "settings" }) { Text(s.text("الإعدادات", "Settings")) }
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                Box(Modifier.widthIn(max = 960.dp).fillMaxSize()) {
                    when (page) {
                        "home" -> HomeScreen(done.size, lessons.size, onPage = { page = it }, onSection = { openGuideSection(it) })
                        "lessons", "favorites" -> CurriculumScreen(lessons, done, favorites, page == "favorites", ::openLesson)
                        "lesson" -> lessons.find { it.id == lessonId }?.let { lesson ->
                            key(lesson.id) { LessonScreen(lesson, lessons, lesson.id in favorites, lesson.id in done, store,
                                onFavorite = {
                                    favorites = if (lesson.id in favorites) favorites - lesson.id else favorites + lesson.id
                                    store.saveFavorites(favorites)
                                }, onComplete = { done = done + lesson.id; store.saveCompleted(done) }, onLesson = ::openLesson) }
                        }
                        "elements" -> ElementsScreen(library.elements) { elementNumber = it; page = "element" }
                        "element" -> library.elements.find { it.number == elementNumber }?.let { key(it.number) { ElementScreen(it) } }
                        "lab" -> LabScreen()
                        "guides" -> GuidesScreen(guides.filter { it.section == guideSection }, guideSection) { guideId = it; page = "guide" }
                        "guide" -> guides.find { it.id == guideId }?.let { key(it.id) { GuideScreen(it) } }
                        "settings" -> SettingsScreen(english) { english = it; store.saveEnglish(it) }
                    }
                }
            }
            NavigationBar {
                val nav = listOf("home" to s.text("الرئيسية", "Home"), "lessons" to s.text("الدروس", "Learn"),
                    "elements" to s.text("العناصر", "Elements"), "lab" to s.text("المختبر", "Lab"), "favorites" to s.text("المفضلة", "Saved"))
                val icons = listOf("⌂", "≡", "He", "⚗", "☆")
                nav.forEachIndexed { i, (id, label) ->
                    NavigationBarItem(selected = page == id || (page == "lesson" && id == "lessons") || (page == "element" && id == "elements"),
                        onClick = { page = id }, icon = { Text(icons[i]) }, label = { Text(label, maxLines = 1) })
                }
            }
        }
    }
}

@Composable private fun HomeScreen(completed: Int, total: Int, onPage: (String) -> Unit, onSection: (String) -> Unit) {
    val s = LocalWords.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Heading(s.text("من الذرة إلى علوم الأرض", "From atoms to Earth science"))
            Text(s.text("تعلم بالعربية والإنجليزية • محتوى محلي دون اتصال", "Learn in Arabic and English • available offline")) }
        item { InfoCard(s.text("مسارك التعليمي", "Your learning path"), s.text("$completed من $total درسًا مكتملًا", "$completed of $total lessons completed"))
            LinearProgressIndicator(progress = { completed.toFloat() / total.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) }
        item { NavCard(s.text("فهرس المنهج", "Curriculum index"), s.text("أربعة مستويات • أهداف ومسائل واختبار لكل درس", "Four levels • objectives, worked examples and lesson quizzes")) { onPage("lessons") } }
        item { NavCard(s.text("الجدول الدوري", "Periodic table"), s.text("118 عنصرًا • شبكة وبحث وخصائص موثقة", "118 elements • grid, search and referenced properties")) { onPage("elements") } }
        item { NavCard(s.text("المحاكاة الآمنة", "Safe simulations"), s.text("حفظ الذرات والتخفيف والغازات والحموضة", "Atom conservation, dilution, gases and acidity")) { onPage("lab") } }
        item { NavCard(s.text("جيولوجيا اليمن", "Geology of Yemen"), s.text("سياق جيولوجي وحدود البيانات المحلية", "Geological context and limits of local evidence")) { onSection("geology") } }
        item { NavCard(s.text("الذهب والمعادن", "Gold and minerals"), s.text("الخامات وأخذ العينات والتحليل وحساب المحتوى", "Ores, sampling, analysis and contained-metal calculations")) { onSection("gold") } }
        item { NavCard(s.text("السلامة المهنية", "Occupational safety"), s.text("تقييم المخاطر والوقاية والاستجابة للطوارئ", "Risk assessment, prevention and emergency response")) { onSection("safety") } }
        item { Text(s.text("الإصدار 4.0 • مادة تعليمية محددة النطاق؛ لا تمثل اعتمادًا مهنيًا.", "Version 4.0 • a bounded educational resource; not professional certification."), style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable private fun SettingsScreen(english: Boolean, onEnglish: (Boolean) -> Unit) {
    val s = LocalWords.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Heading(s.text("الإعدادات والمعلومات", "Settings and information")) }
        item { Row(verticalAlignment = Alignment.CenterVertically) {
            Text("English / العربية", modifier = Modifier.weight(1f)); Switch(checked = english, onCheckedChange = onEnglish)
        }
            TextButton(onClick = { onEnglish(false) }) { Text("العربية") }
            TextButton(onClick = { onEnglish(true) }) { Text("English") }
        }
        item { InfoCard(s.text("الخصوصية والعمل دون اتصال", "Privacy and offline use"), s.text(
            "المحتوى والحسابات محلية. يحفظ الجهاز اللغة والمفضلة والتقدم. فتح المراجع اختياري ويحتاج متصفحًا واتصالًا. لا يطلب التطبيق إذن الإنترنت أو الموقع أو التخزين.",
            "Content and calculations stay on the device. Language, saved lessons and progress are stored locally. References open optionally in a browser and need a connection. The app requests no Internet, location or storage permissions.")) }
        item { InfoCard(s.text("حدود الإصدار", "Scope and limits"), s.text(
            "نماذج ثنائية الأبعاد وحسابات مثالية، وليست محاكاة ثلاثية الأبعاد أو تنبؤًا بتفاعلات عشوائية. لا توجد بيانات عينات معتمدة تثبت مواقع الذهب في كعيدنة وكشر وبني قيس، ولا يغني المحتوى عن دراسة موقع أو ترخيص. يلزم اختبار على أجهزة فعلية قبل النشر العام.",
            "Two-dimensional educational models and ideal calculations, without 3D or prediction of arbitrary reactions. Certified sample data establishing gold occurrences in Kaidana, Kushar and Bani Qais are unavailable here. Site studies and permits remain necessary. Physical-device testing is required before general publication.")) }
        item { InfoCard(s.text("الإصدار والتوقيع", "Version and signing"), s.text(
            "4.0.0 • حزمة اختبار بتوقيع تطوير. للتوزيع المستمر يلزم مفتاح إصدار ثابت لدى مالك المشروع.",
            "4.0.0 • development-signed test package. Continuing distribution requires a stable release key held by the project owner.")) }
    }
}
