package org.arabicchemistry.lab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
internal val LocalReducedMotion = staticCompositionLocalOf { false }
internal val Blue = Color(0xFF08649A)
internal val Ink = Color(0xFF153249)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val loaded by produceState<Result<Library>?>(null) {
                value = withContext(Dispatchers.IO) { runCatching { ContentRepository.load(applicationContext) } }
            }
            ChemTheme(false) {
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
    var mineralId by rememberSaveable { mutableStateOf("") }
    val holder = rememberSaveableStateHolder()
    val appearance = remember { context.getSharedPreferences("appearance", 0) }
    var themeMode by remember { mutableIntStateOf(appearance.getInt("theme", 0)) }
    var reducedMotion by remember { mutableStateOf(appearance.getBoolean("reduce_motion", false)) }
    val dark = when(themeMode) { 1 -> false; 2 -> true; else -> isSystemInDarkTheme() }
    val s = Words(english)
    val lessons = library.lessons(english)
    val guides = library.guides(english)
    fun openLesson(id: String) { if (page != "lesson") lessonReturn = if (page in setOf("favorites", "glossary")) page else "lessons"; lessonId = id; page = "lesson" }
    fun openGuideSection(section: String) { guideSection = section; page = "guides" }
    fun goBack() { page = when (page) { "lesson" -> lessonReturn; "element" -> "elements"; "guide" -> "guides"; "mineral" -> "minerals"; else -> "home" } }
    BackHandler(page != "home") { goBack() }
    ChemTheme(dark) {
    CompositionLocalProvider(LocalWords provides s, LocalReducedMotion provides reducedMotion, LocalLayoutDirection provides if (english) LayoutDirection.Ltr else LayoutDirection.Rtl) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).windowInsetsPadding(WindowInsets.safeDrawing)) {
            Surface(shadowElevation = 3.dp, color = MaterialTheme.colorScheme.surface) {
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
                        "lessons", "favorites" -> holder.SaveableStateProvider("$page-$english") { CurriculumScreen(lessons, done, favorites, page == "favorites", ::openLesson) }
                        "lesson" -> lessons.find { it.id == lessonId }?.let { lesson ->
                            key(lesson.id) { LessonScreen(lesson, lessons, lesson.id in favorites, lesson.id in done, store,
                                onFavorite = {
                                    favorites = if (lesson.id in favorites) favorites - lesson.id else favorites + lesson.id
                                    store.saveFavorites(favorites)
                                }, onComplete = { done = done + lesson.id; store.saveCompleted(done) }, onLesson = ::openLesson) }
                        }
                        "glossary" -> holder.SaveableStateProvider("glossary-$english") { GlossaryScreen(library.glossary, lessons, ::openLesson) }
                        "elements" -> holder.SaveableStateProvider("elements-$english") { ElementsScreen(library.elements) { elementNumber = it; page = "element" } }
                        "element" -> library.elements.find { it.number == elementNumber }?.let { key(it.number) { ElementScreen(it) } }
                        "lab" -> LabScreen(library.elements)
                        "minerals" -> holder.SaveableStateProvider("minerals-$english") { MineralsScreen { mineralId = it; page = "mineral" } }
                        "mineral" -> MineralDetailScreen(mineralId)
                        "guides" -> GuidesScreen(guides.filter { it.section == guideSection }, guideSection) { guideId = it; page = "guide" }
                        "goldjourney" -> GoldJourneyScreen()
                        "guide" -> guides.find { it.id == guideId }?.let { key(it.id) { GuideScreen(it) } }
                        "settings" -> SettingsScreen(english, themeMode, reducedMotion, { english = it; store.saveEnglish(it) }, { themeMode = it; appearance.edit().putInt("theme", it).apply() }, { reducedMotion = it; appearance.edit().putBoolean("reduce_motion", it).apply() })
                    }
                }
            }
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
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

}

@Composable private fun HomeScreen(completed: Int, total: Int, onPage: (String) -> Unit, onSection: (String) -> Unit) {
    val s = LocalWords.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(s.text("كيميا · مختبر الفهم", "KIMYA · A learning lab"), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                    Text(s.text("الكيمياء أقرب مما تتخيل", "Chemistry is closer than you think"), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(s.text("ابدأ من الفكرة، ثم اتبع أثرها في العنصر والخام والتفاعل.", "Start with the idea, then follow it through elements, minerals and reactions."), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .9f), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.text("مسارك التعليمي", "Your learning path"), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(s.text("$completed من $total درسًا مكتملًا", "$completed of $total lessons completed"), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    LinearProgressIndicator(progress = { completed.toFloat() / total.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        item {
            Card(onClick = { onPage("goldjourney") }, Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.text("مسار خاص · الذهب", "Featured path · Gold"), color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelLarge)
                    Text(s.text("افهم رحلة الذهب قبل أي معالجة", "Understand gold before any processing"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(s.text("من الجيولوجيا وتوصيف الخام إلى التركيز والتنقية كمفاهيم صناعية آمنة.", "From geology and ore characterization to safe industrial concepts of concentration and refining."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { NavCard(s.text("الدروس المطوّرة", "Enriched lessons"), s.text("48 درسًا مع خريطة أفكار وأخطاء شائعة وأسئلة تفكير", "48 lessons with idea maps, common mistakes and reflection")) { onPage("lessons") } }
        item { NavCard(s.text("العناصر والجدول الدوري", "Elements and periodic table"), s.text("118 عنصرًا مع بحث وخصائص ومصادر", "118 elements with search, properties and sources")) { onPage("elements") } }
        item { NavCard(s.text("الخامات والمعادن", "Minerals and ores"), s.text("خصائص وتكوّن واستخدامات وتنبيهات سلامة", "Properties, formation, uses and safety")) { onPage("minerals") } }
        item { NavCard(s.text("المختبر والمحاكاة", "Lab and simulations"), s.text("أدوات تعليمية آمنة للحساب والفهم", "Safe educational tools for calculation and understanding")) { onPage("lab") } }
        item { NavCard(s.text("المعجم والأدلة", "Glossary and guides"), s.text("مصطلحات وروابط سياقية تساعدك على التقدم", "Contextual terms and guides to keep learning")) { onPage("glossary") } }
        item { Text(s.text("المحتوى تعليمي؛ أي تجربة فعلية تحتاج مختبرًا مرخصًا وإشرافًا ومعدات حماية.", "Educational content; real experiments require a licensed laboratory, supervision and protective equipment."), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable private fun GoldJourneyScreen() {
    val s = LocalWords.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Heading(s.text("رحلة الذهب العلمية", "The scientific journey of gold")); Text(s.text("أربع محطات من قراءة الخام إلى فهم الفصل والتنقية، دون وصفات تشغيلية خطرة.", "Four stages from reading ore to understanding separation and refining, without hazardous recipes."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { InfoCard(s.text("مختبر آمن أولاً", "Safety first"), s.text("أي معالجة فعلية للخامات تتطلب مختبرًا مرخصًا وتهوية ومعدات حماية وإدارة للمخلفات. لا تخلط أو تسخّن أو تطحن خامًا مجهولًا في المنزل.", "Real ore processing requires a licensed laboratory, ventilation, protective equipment and waste management. Never mix, heat or grind unknown ore at home.")) }
        item { InfoCard(s.text("1 · الجيولوجيا", "1 · Geology"), s.text("يوجد الذهب في عروق مرتبطة بالكوارتز أو في رواسب غرينية. اللون الأصفر أو وجود الكوارتز وحدهما لا يثبتان وجود خام اقتصادي.", "Gold can occur in quartz-associated veins or placer deposits. Yellow colour or quartz alone does not prove an economic ore.")) }
        item { InfoCard(s.text("2 · توصيف العينة", "2 · Sample characterization"), s.text("تبدأ الدراسة المهنية بتوثيق المصدر وتمثيل العينة وفحصها معدنيًا وتحليلها لدى مختبر معتمد قبل اختيار أي مسار.", "Professional study starts with documented provenance, representative sampling, mineralogical examination and accredited analysis.")) }
        item { InfoCard(s.text("3 · التركيز كمفهوم", "3 · Concentration as a concept"), s.text("قد تستخدم المنشآت فروق الكثافة أو تقنيات فصل أخرى مناسبة للخامة. التكسير والطحن والتحكم بالغبار عمليات هندسية وليست وصفة منزلية.", "Facilities may use density differences or other ore-suitable separation methods. Crushing, grinding and dust control are engineering operations, not home recipes.")) }
        item { InfoCard(s.text("4 · التنقية والتحقق", "4 · Refining and verification"), s.text("التنقية الكيميائية الصناعية تتم في دوائر مرخصة بضوابط صارمة. يشرح كيميا مبادئ الفصل والاتزان والتحليل فقط ولا يقدم كميات أو خلطات أو أزمنة تشغيل.", "Industrial chemical refining occurs in licensed circuits with strict controls. Kimya explains separation, equilibrium and analysis only; it provides no quantities, mixtures or operating times.")) }
    }
}

@Composable private fun SettingsScreen(english: Boolean, themeMode: Int, reducedMotion: Boolean, onEnglish: (Boolean) -> Unit, onTheme: (Int) -> Unit, onReducedMotion: (Boolean) -> Unit) {
    val s = LocalWords.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Heading(s.text("الإعدادات والمعلومات", "Settings and information")) }
        item { Row(verticalAlignment = Alignment.CenterVertically) {
            Text("English / العربية", modifier = Modifier.weight(1f)); Switch(checked = english, onCheckedChange = onEnglish)
        }
            TextButton(onClick = { onEnglish(false) }) { Text("العربية") }
            TextButton(onClick = { onEnglish(true) }) { Text("English") }
        }
        item {
            Text(s.text("المظهر", "Appearance"), style = MaterialTheme.typography.titleLarge)
            listOf(s.text("حسب النظام", "System"), s.text("فاتح", "Light"), s.text("داكن", "Dark")).forEachIndexed { i, label ->
                FilterChip(selected = themeMode == i, onClick = { onTheme(i) }, label = { Text(label) })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(s.text("تقليل الحركة", "Reduce motion"), modifier = Modifier.weight(1f))
                Switch(checked = reducedMotion, onCheckedChange = onReducedMotion, modifier = Modifier.semantics { contentDescription = s.text("تقليل الحركة", "Reduce motion") })
            }
        }
        item { InfoCard(s.text("الخصوصية والعمل دون اتصال", "Privacy and offline use"), s.text(
            "المحتوى والحسابات محلية. يحفظ الجهاز اللغة والمفضلة والتقدم. فتح المراجع اختياري ويحتاج متصفحًا واتصالًا. لا يطلب التطبيق إذن الإنترنت أو الموقع أو التخزين.",
            "Content and calculations stay on the device. Language, saved lessons and progress are stored locally. References open optionally in a browser and need a connection. The app requests no Internet, location or storage permissions.")) }
        item { InfoCard(s.text("حدود الإصدار", "Scope and limits"), s.text(
            "نماذج تعليمية وحسابات مثالية وعرض فراغي للجزيئات؛ لا تتنبأ بتفاعلات عشوائية أو حركية جزيئية. لا توجد بيانات عينات معتمدة تثبت مواقع الذهب في كعيدنة وكشر وبني قيس، ولا يغني المحتوى عن دراسة موقع أو ترخيص. يلزم اختبار على أجهزة فعلية قبل النشر العام.",
            "Educational models, ideal calculations and spatial molecular views; no arbitrary reaction prediction or molecular dynamics. Certified sample data establishing gold occurrences in Kaidana, Kushar and Bani Qais are unavailable here. Site studies and permits remain necessary. Physical-device testing is required before general publication.")) }
        item { InfoCard(s.text("الإصدار والتوقيع", "Version and signing"), s.text(
            "4.0.0 • حزمة اختبار بتوقيع تطوير. للتوزيع المستمر يلزم مفتاح إصدار ثابت لدى مالك المشروع.",
            "4.0.0 • development-signed test package. Continuing distribution requires a stable release key held by the project owner.")) }
    }
}
