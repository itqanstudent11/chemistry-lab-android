package org.arabicchemistry.lab

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContentAndPersistenceTest {
    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefs get() = context.getSharedPreferences("chemistry", Context.MODE_PRIVATE)

    @Before fun cleanState() { assertTrue(prefs.edit().clear().commit()) }
    @After fun clearState() { prefs.edit().clear().commit() }

    @Test fun bundledContentLoadsBothLanguagesAndEveryPeriodicElement() {
        val library = ContentRepository.load(context)
        assertTrue(library.lessonsAr.size >= 42)
        assertEquals(library.lessonsAr.map { it.id }, library.lessonsEn.map { it.id })
        assertEquals((1..118).toList(), library.elements.map { it.number })
        assertEquals("Au", library.elements.single { it.number == 79 }.symbol)
        assertEquals(library.guidesAr.map { it.id }, library.guidesEn.map { it.id })
        assertEquals(32, library.glossary.size)
        assertTrue(library.glossary.all { entry -> library.lessonsAr.any { it.id == entry.lessonId } })
        assertEquals(setOf("geology", "gold", "safety"), library.guidesAr.map { it.section }.toSet())
        assertTrue(library.lessonsAr.all { it.questions.size >= 3 && it.objectives.isNotEmpty() })
        assertTrue(library.lessonsEn.all { it.questions.all { q -> q.answer in q.options.indices } })
    }

    @Test fun originalIndexProgressMigratesExactlyOnceWithoutInvalidEntries() {
        val ids = ContentRepository.load(context).lessonsAr.map { it.id }
        assertTrue(prefs.edit().putStringSet("done", setOf("0", "2", "-1", "99999", "broken"))
            .putStringSet("favorites", setOf("1", ids[3], "99999"))
            .putBoolean("english", true).commit())
        val migrated = LearningStore(context, ids)
        assertEquals(setOf(ids[0], ids[2]), migrated.completed())
        assertEquals(setOf(ids[1], ids[3]), migrated.favorites())
        assertTrue(migrated.english())
        // Legacy keys changing later must not overwrite data saved by the upgraded app.
        migrated.saveCompleted(setOf(ids[4]))
        assertTrue(prefs.edit().putStringSet("done", setOf("6")).commit())
        assertEquals(setOf(ids[4]), LearningStore(context, ids).completed())
    }

    @Test fun stableProgressFavoritesLanguageAndQuizPassesSurviveNewStoreInstances() {
        val ids = ContentRepository.load(context).lessonsAr.map { it.id }
        val first = LearningStore(context, ids)
        first.saveCompleted(setOf(ids[0]))
        first.saveFavorites(setOf(ids[1], ids[2]))
        first.saveEnglish(true)
        first.savePassed(ids[1], setOf(0, 2))
        val restored = LearningStore(context, ids)
        assertEquals(setOf(ids[0]), restored.completed())
        assertEquals(setOf(ids[1], ids[2]), restored.favorites())
        assertTrue(restored.english())
        assertEquals(setOf(0, 2), restored.passed(ids[1]))
        assertTrue(restored.passed(ids[2]).isEmpty())
        restored.saveFavorites(setOf(ids[2]))
        assertEquals(setOf(ids[2]), LearningStore(context, ids).favorites())
        // Content removal must not leave impossible entries in displayed progress.
        assertTrue(LearningStore(context, ids.drop(3)).completed().isEmpty())
        assertTrue(LearningStore(context, ids.drop(3)).favorites().isEmpty())
    }
}
