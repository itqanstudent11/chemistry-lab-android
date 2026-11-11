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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import org.json.JSONArray

private val Navy = Color(0xFF091426)
private val Surface = Color(0xFF152B42)
private val Cyan = Color(0xFF5EEAD4)
private val Gold = Color(0xFFF4C66A)
private val White = Color(0xFFF3F8FC)

private data class Lesson(val title:String,val level:String,val intro:String,val detail:String,val example:String,val question:String,val options:List<String>,val answer:Int,val explanation:String)
private data class Element(val number:Int,val symbol:String,val name:String,val mass:String)

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
  val lessons = JSONArray(assets.open("lessons.json").bufferedReader().use { it.readText() }).let { arr -> (0 until arr.length()).map { i -> val o=arr.getJSONObject(i); val opts=o.getJSONArray("options"); Lesson(o.getString("title"),o.getString("level"),o.getString("intro"),o.getString("detail"),o.getString("example"),o.getString("question"),(0 until opts.length()).map{opts.getString(it)},o.getInt("answer"),o.getString("explanation")) } }
  val elements = JSONArray(assets.open("elements.json").bufferedReader().use { it.readText() }).let { arr -> (0 until arr.length()).map { i -> val o=arr.getJSONObject(i); Element(o.getInt("number"),o.getString("symbol"),o.getString("name"),o.getString("mass")) } }
  setContent { ChemistryApp(lessons,elements) }
 }
}

@Composable private fun ChemistryApp(lessons:List<Lesson>,elements:List<Element>) {
 var page by remember { mutableStateOf("home") }
 var selected by remember { mutableIntStateOf(0) }
 var query by remember { mutableStateOf("") }
 var choice by remember { mutableIntStateOf(-1) }
 var showTest by remember { mutableStateOf(false) }
 val done = remember { mutableStateListOf<Int>() }
 BackHandler(page != "home") { page=when(page){"lesson","test"->"lessons";else->"home"};showTest=false }
 MaterialTheme(colorScheme=darkColorScheme(primary=Cyan,secondary=Gold,background=Navy,surface=Surface,onBackground=White,onSurface=White)) {
  Column(Modifier.fillMaxSize().background(Navy).padding(WindowInsets.safeDrawing.asPaddingValues())) {
   Row(Modifier.fillMaxWidth().background(Surface).padding(18.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
    Text("مختبر الكيمياء",fontSize=23.sp,fontWeight=FontWeight.Bold,color=White)
    Text("⚛",fontSize=30.sp,color=Cyan)
   }
   when(page) {
    "home" -> LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
      item { Text("تعلّم. اكتشف. حلّل.",fontSize=27.sp,fontWeight=FontWeight.Bold,color=White); Text("من التأسيس إلى التخصص · دون إنترنت",color=Color(0xFFB5C9D9)) }
      item { NavCard("📚 المسارات التعليمية","دروس متدرجة واختبار بعد نهاية كل درس") { page="lessons" } }
      item { NavCard("⚛️ الجدول الدوري","جميع العناصر الـ118 مع البحث") { page="elements" } }
      item { NavCard("🧪 المختبر الافتراضي","قيد التطوير — لا توجد محاكاة مفعّلة في هذه المعاينة") { page="lab" } }
      item { Text("نسخة معاينة تقنية: 10 دروس تمهيدية، وليست المنهج النهائي الشامل.",color=Gold,fontSize=13.sp) }
    }
    "lessons" -> LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
      item { Heading("المسارات التعليمية"); Text("اختر الدرس واقرأه كاملًا قبل الاختبار",color=Color.LightGray) }
      itemsIndexed(lessons) { i,l -> NavCard("${i+1}. ${l.title}","${l.level}  ·  ${if(i in done) "مكتمل ✓" else "ابدأ القراءة"}") { selected=i;showTest=false;page="lesson" } }
    }
    "lesson" -> { val l=lessons[selected]; LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
      item { Text("${l.level}  /  الدرس ${selected+1}",color=Cyan,fontSize=14.sp); Heading(l.title) }
      item { Section("مقدمة ومفاهيم",l.intro) }
      item { Section("التفسير العلمي",l.detail) }
      item { Section("مثال تطبيقي",l.example) }
      item { Section("خلاصة الدرس","راجع المفاهيم السابقة، ثم انتقل إلى الاختبار للتحقق من فهمك.") }
      item { Button(onClick={choice=-1;page="test"},modifier=Modifier.fillMaxWidth()) { Text("ابدأ اختبار الدرس") } }
      item { TextButton(onClick={page="lessons"}) { Text("العودة إلى الدروس") } }
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
      LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)) { itemsIndexed(filtered) { _,e -> Card(colors=CardDefaults.cardColors(containerColor=Surface),modifier=Modifier.fillMaxWidth()) { Row(Modifier.padding(14.dp).fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) { Text("${e.number}  ${e.name}",color=White,fontSize=17.sp); Column(horizontalAlignment=Alignment.CenterHorizontally) { Text(e.symbol,color=Gold,fontSize=25.sp,fontWeight=FontWeight.Bold); Text("${e.mass} u",color=Color.LightGray,fontSize=12.sp) } } } } }
    }
    "lab" -> Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) { Heading("المختبر الافتراضي"); Text("المحاكاة ثلاثية الأبعاد والتجارب العلمية ليست جاهزة بعد. هذه شاشة صريحة بالحالة الحالية وليست محاكاة وهمية.",color=White); Button(onClick={page="home"}){Text("الرئيسية")} }
   }
  }
 }
}
@Composable private fun Heading(s:String){ Text(s,fontSize=25.sp,fontWeight=FontWeight.Bold,color=White,lineHeight=34.sp) }
@Composable private fun Section(title:String,body:String){ Card(colors=CardDefaults.cardColors(containerColor=Surface),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){ Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text(title,fontSize=20.sp,fontWeight=FontWeight.SemiBold,color=Cyan);Text(body,fontSize=17.sp,lineHeight=29.sp,color=White)} } }
@Composable private fun NavCard(title:String,subtitle:String,onClick:()->Unit){ Card(onClick=onClick,colors=CardDefaults.cardColors(containerColor=Surface),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){ Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(title,color=White,fontSize=21.sp,fontWeight=FontWeight.Bold);Text(subtitle,color=Color(0xFFB7CBDA),fontSize=15.sp,lineHeight=23.sp)} } }
