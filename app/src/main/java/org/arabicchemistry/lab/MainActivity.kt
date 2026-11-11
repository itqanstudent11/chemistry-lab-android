package org.arabicchemistry.lab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import org.json.JSONArray

private val Navy = Color(0xFFF1F7FC)
private val Surface = Color(0xFFFFFFFF)
private val Cyan = Color(0xFF086BBA)
private val Gold = Color(0xFF936000)
private val White = Color(0xFF142C49)

private data class Lesson(val title:String,val level:String,val intro:String,val detail:String,val example:String,val question:String,val options:List<String>,val answer:Int,val explanation:String,val section:String,val summary:String,val source:String)
private data class Element(val number:Int,val symbol:String,val name:String,val mass:String)

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
  val lessons = JSONArray(assets.open("lessons.json").bufferedReader().use { it.readText() }).let { arr -> (0 until arr.length()).map { i -> val o=arr.getJSONObject(i); val opts=o.getJSONArray("options"); Lesson(o.getString("title"),o.getString("level"),o.getString("intro"),o.getString("detail"),o.getString("example"),o.getString("question"),(0 until opts.length()).map{opts.getString(it)},o.getInt("answer"),o.getString("explanation"),o.optString("section","الكيمياء العامة"),o.optString("summary"),o.optString("source")) } }
  val elements = JSONArray(assets.open("elements.json").bufferedReader().use { it.readText() }).let { arr -> (0 until arr.length()).map { i -> val o=arr.getJSONObject(i); Element(o.getInt("number"),o.getString("symbol"),o.getString("name"),o.getString("mass")) } }
  setContent { ChemistryApp(lessons,elements) }
 }
}

@Composable private fun ChemistryApp(lessons:List<Lesson>,elements:List<Element>) {
 var page by remember { mutableStateOf("home") }
 var selected by remember { mutableIntStateOf(0) }
 var elementSelected by remember { mutableIntStateOf(0) }
 var query by remember { mutableStateOf("") }
 var choice by remember { mutableIntStateOf(-1) }
 var showTest by remember { mutableStateOf(false) }
 val prefs = LocalContext.current.getSharedPreferences("chemistry",0)
 val done = remember { mutableStateListOf<Int>().apply { addAll(prefs.getStringSet("done", emptySet())!!.mapNotNull { it.toIntOrNull() }) } }
 var menu by remember { mutableStateOf(false) }
 var favorites by remember { mutableStateOf(prefs.getStringSet("favorites",emptySet())!!.toSet()) }
 LaunchedEffect(favorites) { prefs.edit().putStringSet("favorites",favorites).apply() }
 var level by remember { mutableStateOf("") }
 var english by remember { mutableStateOf(prefs.getBoolean("english",false)) }
 LaunchedEffect(english) { prefs.edit().putBoolean("english",english).apply() }
 LaunchedEffect(done.toList()) { prefs.edit().putStringSet("done",done.map{it.toString()}.toSet()).apply() }
 BackHandler(page != "home") { page=when(page){"lesson","test"->"lessons";"element"->"elements";else->"home"};showTest=false }
 MaterialTheme(colorScheme=lightColorScheme(primary=Cyan,secondary=Gold,background=Navy,surface=Surface,onBackground=White,onSurface=White)) {
  Column(Modifier.fillMaxSize().background(Navy).padding(WindowInsets.safeDrawing.asPaddingValues())) {
   Row(Modifier.fillMaxWidth().background(Surface).padding(18.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
    Text(if(english) "Chemistry Lab" else "مختبر الكيمياء",fontSize=23.sp,fontWeight=FontWeight.Bold,color=White)
    Box { TextButton(onClick={menu=true}) { Text("☰",fontSize=28.sp,color=Cyan) }; DropdownMenu(expanded=menu,onDismissRequest={menu=false}) { DropdownMenuItem(text={Text("الإعدادات")},onClick={menu=false;page="settings"}); DropdownMenuItem(text={Text("الذهب وتحليل الخامات")},onClick={menu=false;page="gold"}) } }
   }
   Box(Modifier.weight(1f)) { when(page) {
    "home" -> LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
      item { Card(shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFFDDEEFF))) { Row(Modifier.fillMaxWidth().padding(20.dp),verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("تعلّم الكيمياء بالتجربة والتفاعل",fontSize=26.sp,fontWeight=FontWeight.Bold,color=White); Text("اكتشف عالم المادة",color=Cyan); Button(onClick={page="lessons"}){Text("ابدأ التعلم")} }; ScienceArt(Modifier.size(112.dp),Cyan) } } }
      item { Section("مسارك التعليمي", "${done.size} / ${lessons.size} دروس مكتملة"); LinearProgressIndicator(progress={done.size.toFloat()/lessons.size.coerceAtLeast(1)},modifier=Modifier.fillMaxWidth().padding(top=8.dp)) }
      item { Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) { Tile("المسارات التعليمية","دروس متدرجة",Color(0xFFE9E2FF),Modifier.weight(1f)){page="lessons"}; Tile("الجدول الدوري","118 عنصرًا",Color(0xFFD8F1FF),Modifier.weight(1f)){page="elements"} } }
      item { Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) { Tile("المختبر الافتراضي","محاكاة التخفيف",Color(0xFFDDF5F0),Modifier.weight(1f)){page="lab"}; Tile("الذهب والمعادن","تحليل الخامات",Color(0xFFFFEBC8),Modifier.weight(1f)){page="gold"} } }
      item { Text("إصدار تطويري • يعمل دون إنترنت",color=Cyan,fontSize=13.sp) }
    }
    "lessons" -> LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
      item { Heading("المسارات التعليمية"); Text("اختر الدرس واقرأه كاملًا قبل الاختبار",color=Color(0xFF526A82)) }
      item { Section("فهرس المنهج", "تأسيسي ← متوسط ← جامعي ← تخصصي. اختر المستوى لعرض فهرس دروسه.") }
      item { Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) { listOf("", "تأسيسي","متوسط","جامعي","تخصصي").forEach { k -> FilterChip(selected=level==k,onClick={level=k},label={Text(k.ifBlank { "الكل" })}) } } }
      lessons.withIndex().filter { level.isBlank() || it.value.level.replace("متخصص","تخصصي")==level }.groupBy { it.value.section }.forEach { (group, entries) ->
       item { Heading(group) }
       itemsIndexed(entries) { _,entry -> val i=entry.index; val l=entry.value; NavCard("${i+1}. ${l.title}",if(i in done) "مكتمل ✓" else "ابدأ القراءة") { selected=i;choice=-1;showTest=false;page="lesson" } }
      }
    }
    "lesson" -> { val l=lessons[selected]; LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
      item { Text("${l.level}  /  الدرس ${selected+1}",color=Cyan,fontSize=14.sp); Heading(l.title); Text(l.section); Section("هدف التعلم","فهم ${l.title} وتطبيق المفاهيم في المثال المحلول.") }
      item { Section("فهرس الدرس","١. مقدمة ومفاهيم\n٢. التفسير العلمي\n٣. مثال تطبيقي\n٤. خلاصة الدرس\n٥. اختبار نهاية الدرس"); TextButton(onClick={val k=selected.toString(); favorites=if(k in favorites) favorites-k else favorites+k}) { Text(if(selected.toString() in favorites) "إزالة من المفضلة" else "إضافة إلى المفضلة") } }
      item { Section("مقدمة ومفاهيم",l.intro) }
      item { Section("التفسير العلمي",l.detail) }
      item { Section("مثال تطبيقي",l.example) }
      item { Section("خلاصة الدرس",l.summary); Section("مرجع للمراجعة",l.source) }
      item { Column(verticalArrangement=Arrangement.spacedBy(8.dp)){ Text("اختبار نهاية الدرس",fontWeight=FontWeight.Bold,fontSize=20.sp); Text(l.question); l.options.forEachIndexed { i,opt -> OutlinedButton(onClick={choice=i;if(i==l.answer && selected !in done)done.add(selected)},modifier=Modifier.fillMaxWidth()){Text(opt)} }; if(choice>=0) Section(if(choice==l.answer) "إجابة صحيحة" else "حاول مجددًا",if(choice==l.answer) l.explanation else "راجع الشرح ثم أعد المحاولة.") } }
      item { Row { TextButton(onClick={if(selected>0){selected--;choice=-1}},enabled=selected>0){Text("السابق")}; TextButton(onClick={page="lessons"}) { Text("الفهرس") }; TextButton(onClick={if(selected<lessons.lastIndex){selected++;choice=-1}},enabled=selected<lessons.lastIndex){Text("التالي")} } }
    } }
    "test" -> { val l=lessons[selected]; LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
      item { Heading("اختبار: ${l.title}"); Text(l.question,fontSize=20.sp,color=White) }
      itemsIndexed(l.options) { i,opt -> OutlinedButton(onClick={if(choice==-1) {choice=i; if(i==l.answer && selected !in done)done.add(selected)}},modifier=Modifier.fillMaxWidth(),enabled=choice==-1) { Text(opt,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Right) } }
      if(choice!=-1) item { Section(if(choice==l.answer) "إجابة صحيحة ✓" else "إجابة غير صحيحة",l.explanation); Button(onClick={page="lessons"},modifier=Modifier.fillMaxWidth()) { Text("العودة إلى الدروس") } }
    } }
    "elements" -> Column(Modifier.fillMaxSize().padding(16.dp)) {
      Heading("الجدول الدوري · 118 عنصرًا")
      OutlinedTextField(value=query,onValueChange={query=it},label={Text("ابحث بالاسم أو الرمز أو العدد الذري")},modifier=Modifier.fillMaxWidth(),singleLine=true)
      Spacer(Modifier.height(12.dp))
      val filtered=elements.filter{ query.isBlank() || it.name.contains(query,true)||it.symbol.contains(query,true)||it.number.toString()==query.trim() }
      LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)) { itemsIndexed(filtered) { _,e -> Card(onClick={elementSelected=e.number-1;page="element"},colors=CardDefaults.cardColors(containerColor=Surface),modifier=Modifier.fillMaxWidth()) { Row(Modifier.padding(14.dp).fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) { Text("${e.number}  ${e.name}",color=White,fontSize=17.sp); Column(horizontalAlignment=Alignment.CenterHorizontally) { Text(e.symbol,color=Gold,fontSize=25.sp,fontWeight=FontWeight.Bold); Text("${e.mass} u",color=Color(0xFF526A82),fontSize=12.sp) } } } } }
    }
    "element" -> { val e=elements[elementSelected]; LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) { item { Heading("${e.name} · ${e.symbol}"); Section("الخصائص الأساسية","العدد الذري: ${e.number}\nالكتلة الذرية المدرجة: ${e.mass} u"); Section("التواجد في اليمن","لم تُدمج بعد بيانات محلية موثقة لهذا العنصر. لا يعني ذلك عدم وجوده في اليمن."); TextButton(onClick={page="elements"}){Text("العودة إلى العناصر")} } } }
    "favorites" -> LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) { item { Heading("المفضلة") }; itemsIndexed(lessons.withIndex().filter{it.index.toString() in favorites}) { _,entry -> NavCard(entry.value.title,entry.value.level){selected=entry.index;choice=-1;page="lesson"} } }
    "lab" -> SafeLab()
    "gold" -> GoldEducation()
    "settings" -> Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) { Heading("الإعدادات"); Section("اللغة","تجربة عنوان إنجليزي فقط؛ المحتوى ما زال عربيًا."); Row { TextButton(onClick={english=false}){Text("العربية")}; TextButton(onClick={english=true}){Text("English (preview)")} }; Section("المظهر","المظهر الفاتح"); Button(onClick={page="home"}){Text("الرئيسية")} }

   } }
   NavigationBar(containerColor=Surface) { listOf("home" to "الرئيسية","lessons" to "الدروس","elements" to "العناصر","lab" to "المختبر","favorites" to "المفضلة").forEach { (id,label) -> NavigationBarItem(selected=page==id,onClick={page=id},icon={ScienceArt(Modifier.size(24.dp),Cyan)},label={Text(label,fontSize=11.sp)}) } }
  }
 }
}
@Composable private fun Heading(s:String){ Text(s,fontSize=25.sp,fontWeight=FontWeight.Bold,color=White,lineHeight=34.sp) }
@Composable private fun Section(title:String,body:String){ Card(colors=CardDefaults.cardColors(containerColor=Surface),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){ Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text(title,fontSize=20.sp,fontWeight=FontWeight.SemiBold,color=Cyan);Text(body,fontSize=17.sp,lineHeight=29.sp,color=White)} } }
@Composable private fun NavCard(title:String,subtitle:String,onClick:()->Unit){ Card(onClick=onClick,colors=CardDefaults.cardColors(containerColor=Surface),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){ Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(title,color=White,fontSize=21.sp,fontWeight=FontWeight.Bold);Text(subtitle,color=Color(0xFF526A82),fontSize=15.sp,lineHeight=23.sp)} } }

@Composable private fun ScienceArt(modifier:Modifier,color:Color){ Canvas(modifier){ val r=size.minDimension/2; val c=center; drawCircle(color.copy(alpha=.10f),r,c); for(i in 0..5){val a=i*Math.PI/3;val p=Offset(c.x+(kotlin.math.cos(a)*r*.72).toFloat(),c.y+(kotlin.math.sin(a)*r*.72).toFloat());drawLine(color.copy(alpha=.6f),c,p,strokeWidth=r*.06f);drawCircle(color,r*.15f,p);drawCircle(Color.White.copy(alpha=.8f),r*.05f,p-Offset(r*.04f,r*.04f))};drawCircle(color,r*.24f,c) } }
@Composable private fun Tile(title:String,sub:String,color:Color,modifier:Modifier,onClick:()->Unit){Card(onClick=onClick,modifier=modifier.heightIn(min=160.dp),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=color)){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){ScienceArt(Modifier.size(58.dp),if(color==Color(0xFFFFEBC8))Gold else Cyan);Text(title,fontSize=18.sp,fontWeight=FontWeight.Bold,color=White);Text(sub,fontSize=13.sp,color=White)}}}
@Composable private fun Calculator(gold:Boolean){var a by remember(gold){mutableStateOf("")};var b by remember(gold){mutableStateOf("")};var c by remember(gold){mutableStateOf("")};val x=a.toDoubleOrNull();val y=b.toDoubleOrNull();val z=c.toDoubleOrNull();LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{Heading(if(gold)"الذهب وتحليل الخامات" else "محاكاة التخفيف");ScienceArt(Modifier.size(100.dp),if(gold)Gold else Cyan)};item{Section(if(gold)"الميزان الكتلي" else "حفظ كمية المذاب",if(gold)"المحتوى النظري = كتلة الخام بالطن × التركيز بالجرام لكل طن. الاسترداد تقدير حسابي وليس إجراء استخلاص." else "C₁V₁ = C₂V₂. استخدم نفس وحدة الحجم.")};item{OutlinedTextField(a,{a=it},label={Text(if(gold)"كتلة الخام (طن)" else "التركيز الأول (M)")},modifier=Modifier.fillMaxWidth());OutlinedTextField(b,{b=it},label={Text(if(gold)"التركيز (g/t)" else "الحجم الأول (mL)")},modifier=Modifier.fillMaxWidth());OutlinedTextField(c,{c=it},label={Text(if(gold)"الاسترداد المفترض (%)" else "الحجم النهائي (mL)")},modifier=Modifier.fillMaxWidth())};item{if(x!=null&&y!=null&&z!=null&&x.isFinite()&&y.isFinite()&&z.isFinite()&&x>=0&&y>=0&&((gold&&z in 0.0..100.0)||(!gold&&z>0&&z>=y))){Section("النتيجة",if(gold)"المحتوى النظري: ${x*y} g\nالمسترد افتراضيًا: ${x*y*z/100} g" else "C₂ = ${x*y/z} M")}else Text("أدخل قيمًا رقمية صالحة")};if(gold)item{Section("تقنيات التحليل","XRF للتحليل العنصري، وAAS وICP لقياس التركيز وفق معايرة وتحضير مخبري معتمد. اللون وحده لا يثبت وجود الذهب.")}}}

@Composable private fun SafeLab(){
 var mode by remember { mutableIntStateOf(0) }; var a by remember { mutableIntStateOf(2) }; var b by remember { mutableIntStateOf(1) }
 Column(Modifier.fillMaxSize().padding(16.dp)) {
 Heading("المختبر التفاعلي"); Text("الفهرس: حفظ الذرات • الذوبان • التخفيف",color=Cyan)
 Row { listOf("حفظ الذرات","الذوبان","التخفيف").forEachIndexed { i,t -> TextButton(onClick={mode=i}){Text(t)} } }
 if(mode==2) Calculator(false) else LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp)) {
 item { Section(if(mode==0) "نموذج تكوين الماء" else "ذوبان السكر في الماء",if(mode==0) "محاكاة رقمية فقط: 2H₂ + O₂ → 2H₂O. لا تنفذ التفاعل عمليًا." else "نموذج جسيمي مبسط: جزيئات السكر تتوزع بين جزيئات الماء دون تحولها إلى مادة جديدة.") }
 item { Text(if(mode==0) "عدد جزيئات الهيدروجين: $a" else "عدد جسيمات السكر: $a"); Slider(value=a.toFloat(),onValueChange={a=it.toInt()},valueRange=0f..10f,steps=9); Text(if(mode==0) "عدد جزيئات الأكسجين: $b" else "حجم الماء النسبي: $b"); Slider(value=b.toFloat(),onValueChange={b=it.toInt()},valueRange=1f..10f,steps=8) }
 item { val n=if(mode==0) minOf(a/2,b)*2 else a; Canvas(Modifier.fillMaxWidth().height(180.dp).background(Color(0xFFE0F0FA),RoundedCornerShape(18.dp))){ repeat(n){ i -> val x=size.width*(i%5+1)/6;val y=size.height*(i/5+1)/3;drawCircle(Cyan,12.dp.toPx(),Offset(x,y));if(mode==0){drawCircle(Color.White,6.dp.toPx(),Offset(x-12.dp.toPx(),y+10.dp.toPx()));drawCircle(Color.White,6.dp.toPx(),Offset(x+12.dp.toPx(),y+10.dp.toPx()))} } }; Section("النتيجة",if(mode==0) "الماء الناتج: $n جزيئات. المتبقي H₂: ${a-n}، O₂: ${b-n/2}." else "التركيز النسبي: ${a.toDouble()/b}. نموذج لا يمثل حدود الذوبانية.") }
 } }
}
@Composable private fun GoldEducation(){ var calc by remember { mutableStateOf(false) }; if(calc){Column{TextButton(onClick={calc=false}){Text("العودة إلى دليل الذهب")};Calculator(true)}}else LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
 item{Heading("الذهب والمعادن والسلامة");Section("فهرس القسم","١. حاسبة المحتوى\n٢. جيولوجيا الخامات\n٣. محافظة حجة\n٤. مخاطر السيانيد والرصاص\n٥. غبار المناجم\n٦. الترخيص والرقابة\n٧. المراجع");NavCard("حاسبة محتوى الذهب","التركيز والكتلة"){calc=true}}
 item{Section("جيولوجيا الخامات","قد يوجد الذهب حرًا أو مرتبطًا بمعادن أخرى. لا يثبت اللون أو اللمعان وجود الذهب. تحديد التركيب يحتاج تحليلًا مخبريًا.")}
 item{Section("محافظة حجة","كعيدنة وكشر وبني قيس: مناطق مطلوبة للدراسة. لم تُتحقق بعد بيانات المواقع أو نتائج العينات من مصادر جيولوجية أولية.")}
 item{Section("السيانيد ونترات الرصاص","السيانيد سام حتى عند مراقبة الحموضة. نترات الرصاص مادة سامة ومؤكسدة، والتعرض للرصاص قد يضر الجهاز العصبي والكلى. لا يتضمن التطبيق إجراءات تشغيل الأحواض.")}
 item{Section("غبار المناجم","استنشاق السيليكا البلورية قد يسبب السحار السيليسي وسرطان الرئة. يلزم تقييم التعرض بواسطة مختصين.")}
 item{Section("الترخيص والرقابة","راجع الجهات المختصة لتحديد التراخيص والموافقات البيئية واعتماد التصميم وخطط الطوارئ. لا تعد هذه الصفحة تصريحًا.")}
 item{Section("مراجع السلامة","CDC: https://www.cdc.gov/chemical-emergencies/chemical-fact-sheets/cyanide.html\nWHO: https://www.who.int/news-room/fact-sheets/detail/lead-poisoning-and-health\nNIOSH: https://www.cdc.gov/niosh/")}
 }}
