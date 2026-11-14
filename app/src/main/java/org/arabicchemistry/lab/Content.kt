package org.arabicchemistry.lab

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Question(val question: String, val options: List<String>, val answer: Int, val explanation: String)
data class Lesson(
    val id: String, val title: String, val level: String, val section: String,
    val intro: String, val detail: String, val example: String, val summary: String,
    val objectives: List<String>, val prerequisites: List<String>, val questions: List<Question>,
    val source: String, val sourceUrls: List<String>,
    val whyMatters: String = "", val keyIdeas: List<String> = emptyList(),
    val commonMistakes: List<String> = emptyList(), val safePractice: String = "",
    val reflection: List<String> = emptyList(), val connections: List<String> = emptyList()
)
data class Element(
    val number: Int, val symbol: String, val name: String, val nameEn: String, val mass: String,
    val period: Int, val group: Int, val category: String, val block: String,
    val stateAr: String, val stateEn: String, val descriptionAr: String, val descriptionEn: String,
    val sourceUrl: String, val massKind: String, val massUncertainty: String, val massSourceUrl: String
)
data class Topic(val id: String, val title: String, val body: String)
data class Reference(val title: String, val url: String)
data class Guide(val id: String, val section: String, val title: String, val intro: String,
    val topics: List<Topic>, val sources: List<Reference>, val reviewStatus: String)
data class GlossaryEntry(val id: String, val ar: String, val en: String, val definitionAr: String, val definitionEn: String, val lessonId: String)
data class Library(val lessonsAr: List<Lesson>, val lessonsEn: List<Lesson>, val elements: List<Element>,
    val guidesAr: List<Guide>, val guidesEn: List<Guide>, val glossary: List<GlossaryEntry>) {
    fun lessons(english: Boolean) = if (english) lessonsEn else lessonsAr
    fun guides(english: Boolean) = if (english) guidesEn else guidesAr
}

private fun JSONObject.strings(key: String): List<String> = optJSONArray(key)?.let { a ->
    (0 until a.length()).map { a.getString(it) }
} ?: emptyList()
private fun <T> JSONArray.objects(mapper: (JSONObject) -> T): List<T> =
    (0 until length()).map { mapper(getJSONObject(it)) }

object ContentRepository {
    fun load(context: Context): Library {
        fun array(name: String) = JSONArray(context.assets.open(name).bufferedReader().use { it.readText() })
        fun lessons(name: String) = array(name).objects { o ->
            Lesson(o.getString("id"), o.getString("title"), o.getString("level"), o.getString("section"),
                o.getString("intro"), o.getString("detail"), o.getString("example"), o.getString("summary"),
                o.strings("objectives"), o.strings("prerequisites"),
                o.getJSONArray("questions").objects { q -> Question(q.getString("question"), q.strings("options"), q.getInt("answer"), q.getString("explanation")) },
                o.optString("source"), o.strings("sourceUrls"),
                o.optString("whyMatters"), o.strings("keyIdeas"), o.strings("commonMistakes"),
                o.optString("safePractice"), o.strings("reflection"), o.strings("connections"))
        }
        fun guides(name: String) = array(name).objects { o ->
            Guide(o.getString("id"), o.getString("section"), o.getString("title"), o.getString("intro"),
                o.getJSONArray("topics").objects { Topic(it.getString("id"), it.getString("title"), it.getString("body")) },
                o.getJSONArray("sources").objects { Reference(it.getString("title"), it.getString("url")) }, o.getString("reviewStatus"))
        }
        return Library(lessons("lessons.json"), lessons("lessons_en.json"), array("elements.json").objects { o ->
            Element(o.getInt("number"), o.getString("symbol"), o.getString("name"), o.getString("nameEn"), o.getString("mass"),
                o.getInt("period"), o.getInt("group"), o.getString("category"), o.getString("block"),
                o.getString("stateAr"), o.getString("stateEn"), o.getString("descriptionAr"), o.getString("descriptionEn"), o.getString("sourceUrl"), o.getString("massKind"), o.optString("massUncertainty"), o.getString("massSourceUrl"))
        }, guides("guides_ar.json"), guides("guides_en.json"), array("glossary.json").objects { o ->
            GlossaryEntry(o.getString("id"), o.getString("ar"), o.getString("en"), o.getString("definitionAr"), o.getString("definitionEn"), o.getString("lessonId"))
        })
    }
}

/** Stable content IDs; migrate the original index-based progress exactly once. */
class LearningStore(context: Context, private val lessonIds: List<String>) {
    private val prefs = context.getSharedPreferences("chemistry", Context.MODE_PRIVATE)
    init {
        if (!prefs.getBoolean("stable_ids_v1", false)) {
            fun migrate(key: String) = prefs.getStringSet(key, emptySet()).orEmpty().mapNotNull { old ->
                if (old in lessonIds) old else old.toIntOrNull()?.let { lessonIds.getOrNull(it) }
            }.toSet()
            prefs.edit().putStringSet("completed_ids", migrate("done"))
                .putStringSet("favorite_ids", migrate("favorites")).putBoolean("stable_ids_v1", true).apply()
        }
    }
    fun completed() = prefs.getStringSet("completed_ids", emptySet()).orEmpty().intersect(lessonIds.toSet())
    fun favorites() = prefs.getStringSet("favorite_ids", emptySet()).orEmpty().intersect(lessonIds.toSet())
    fun saveCompleted(ids: Set<String>) { prefs.edit().putStringSet("completed_ids", ids.toSet()).apply() }
    fun saveFavorites(ids: Set<String>) { prefs.edit().putStringSet("favorite_ids", ids.toSet()).apply() }
    fun english() = prefs.getBoolean("english", false)
    fun saveEnglish(value: Boolean) { prefs.edit().putBoolean("english", value).apply() }
    fun passed(id: String) = prefs.getStringSet("quiz_$id", emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.toSet()
    fun savePassed(id: String, indices: Set<Int>) { prefs.edit().putStringSet("quiz_$id", indices.map(Int::toString).toSet()).apply() }
}
