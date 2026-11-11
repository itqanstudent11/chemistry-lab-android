package org.arabicchemistry.lab

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Launches the real activity after clearing settings, including asynchronous asset loading. */
@RunWith(AndroidJUnit4::class)
class AppNavigationTest {
    @get:Rule val compose = createEmptyComposeRule()
    private lateinit var scenario: ActivityScenario<MainActivity>
    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before fun launchArabicApp() {
        assertTrue(context.getSharedPreferences("chemistry", Context.MODE_PRIVATE).edit().clear().commit())
        context.getSharedPreferences("appearance", Context.MODE_PRIVATE).edit().clear().commit()
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitUntil(60_000) { compose.onAllNodesWithText("مختبر الكيمياء").fetchSemanticsNodes().isNotEmpty() }
    }
    @After fun closeApp() {
        if (::scenario.isInitialized) scenario.close()
        context.getSharedPreferences("chemistry", Context.MODE_PRIVATE).edit().clear().commit()
    }

    private fun scrollToText(value: String) {
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(value))
    }

    @Test fun arabicToEnglishLibraryQuizElementSearchAndDilutionWorkAfterRestart() {
        val library = ContentRepository.load(context)
        compose.onNodeWithText("الدروس").performClick()
        compose.onNodeWithText("فهرس المنهج").assertIsDisplayed()
        val arabicTitle = library.lessonsAr.first().title
        compose.onNode(hasSetTextAction()).performTextInput(arabicTitle)
        closeSoftKeyboard()
        val lessonTitleNode = hasText(arabicTitle) and !hasSetTextAction()
        compose.onNodeWithTag("lesson-list").performScrollToNode(lessonTitleNode)
        compose.onNode(lessonTitleNode).assertIsDisplayed()
        compose.onNodeWithText("الإعدادات").performClick()
        compose.onNodeWithText("English").performClick()
        compose.onNodeWithText("Chemistry Lab").assertIsDisplayed()
        compose.onNodeWithText("Settings and information").assertIsDisplayed()

        compose.onNodeWithText("Learn").performClick()
        compose.onNodeWithText("Curriculum index").assertIsDisplayed()
        compose.onNodeWithText("Search the curriculum").assertExists()
        val lesson = library.lessonsEn.first()
        val firstLesson = lesson.title
        scrollToText(firstLesson)
        compose.onNodeWithText(firstLesson).performClick()
        compose.onNodeWithText(firstLesson).assertIsDisplayed()
        compose.onNodeWithText("Save lesson").performClick()
        compose.onNodeWithText("Remove from saved").assertIsDisplayed()

        // A wrong answer must not count as a pass; every question must be answered correctly.
        val firstQuestion = lesson.questions.first()
        val wrongOption = firstQuestion.options.indices.first { it != firstQuestion.answer }
        val wrongTag = "quiz-${lesson.id}-0-$wrongOption"
        compose.onNodeWithTag("lesson-content").performScrollToNode(hasTestTag(wrongTag))
        compose.onNodeWithTag(wrongTag).performClick()
        assertTrue(LearningStore(context, listOf(lesson.id)).passed(lesson.id).isEmpty())
        lesson.questions.forEachIndexed { index, question ->
            val tag = "quiz-${lesson.id}-$index-${question.answer}"
            compose.onNodeWithTag("lesson-content").performScrollToNode(hasTestTag(tag))
            compose.onNodeWithTag(tag).performClick()
        }
        assertTrue(lesson.id in LearningStore(context, listOf(lesson.id)).completed())
        compose.onNodeWithText("Saved").performClick()
        scrollToText(firstLesson)
        compose.onNodeWithText(firstLesson).assertIsDisplayed()

        compose.onNodeWithText("Elements").performClick()
        compose.onNodeWithText("Periodic table • 118 elements").assertIsDisplayed()
        compose.onNodeWithContentDescription("1 Hydrogen H").performClick()
        compose.onNodeWithText("H • Hydrogen").assertIsDisplayed()
        compose.onNodeWithText("Back").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("٧٩")
        closeSoftKeyboard()
        compose.onNodeWithText("1 results").assertIsDisplayed()
        compose.onNodeWithText("79 • Au • Gold").performClick()
        compose.onNodeWithText("Au • Gold").assertIsDisplayed()
        compose.onNodeWithText("Basic properties").assertIsDisplayed()

        compose.onNodeWithText("Lab").performClick()
        compose.onNodeWithText("Safe virtual laboratory").assertIsDisplayed()
        compose.onNodeWithText("Atom conservation").assertExists()
        scrollToText("2H₂ + O₂ → 2H₂O")
        compose.onNodeWithText("2H₂ + O₂ → 2H₂O").assertIsDisplayed()
        compose.onNodeWithText("Dilution").performScrollTo().performClick()
        val inputs = listOf("Initial concentration C₁ (mol/L)" to "١", "Initial volume V₁ (mL)" to "١٠٠", "Final volume V₂ (mL)" to "٢٠٠")
        inputs.forEach { (label, value) ->
            scrollToText(label)
            compose.onNode(hasSetTextAction() and hasText(label)).performTextInput(value)
            closeSoftKeyboard()
        }
        scrollToText("C₂ = 0.5 mol/L")
        compose.onNodeWithText("C₂ = 0.5 mol/L").assertIsDisplayed()

        // Activity recreation verifies persisted language and saved lessons through a real lifecycle.
        scenario.recreate()
        compose.waitUntil(60_000) { compose.onAllNodesWithText("Chemistry Lab").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Saved").performClick()
        scrollToText(firstLesson)
        compose.onNodeWithText(firstLesson).assertIsDisplayed()
    }

    @Test fun arabicRegionalGuideIndexesOpenTheirContent() {
        val library = ContentRepository.load(context)
        for ((section, title) in listOf("geology" to "جيولوجيا اليمن", "gold" to "الذهب والمعادن", "safety" to "السلامة المهنية")) {
            compose.onNodeWithText("الرئيسية").performClick()
            scrollToText(title)
            compose.onNodeWithText(title).performClick()
            val guide = library.guidesAr.first { it.section == section }
            scrollToText(guide.title)
            compose.onNodeWithText(guide.title).performClick()
            compose.onNodeWithText(guide.title).assertIsDisplayed()
            scrollToText(guide.topics.first().title)
            compose.onAllNodesWithText(guide.topics.first().title).onFirst().assertExists()
        }
    }
    @Test fun glossaryAllSafeModelsAndGoldCalculatorWork() {
        val library = ContentRepository.load(context)
        compose.onNodeWithText("الإعدادات").performClick()
        compose.onNodeWithText("English").performClick()
        compose.onNodeWithText("Home").performClick()
        scrollToText("Chemistry glossary")
        compose.onNodeWithText("Chemistry glossary").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("mole")
        closeSoftKeyboard()
        scrollToText("Mole")
        compose.onNodeWithText("Mole").assertIsDisplayed()
        val lesson = library.lessonsEn.single { it.id == "lesson-005" }
        val link = "Lesson and reference: " + lesson.title
        compose.onAllNodesWithText(link).onFirst().performScrollTo().performClick()
        compose.onNodeWithText(lesson.title).assertIsDisplayed()
        compose.onNodeWithText("Lab").performClick()
        fun select(title: String) {
            compose.waitForIdle()
            try {
                compose.onNodeWithText(title).performScrollTo().performClick()
            } catch (first: AssertionError) {
                compose.onNodeWithText("Lab").performClick()
                compose.waitForIdle()
                compose.onNodeWithText(title).performScrollTo().performClick()
            }
        }
        fun fill(label: String, value: String) {
            scrollToText(label)
            compose.onNode(hasSetTextAction() and hasText(label)).performScrollTo().performTextReplacement(value)
            closeSoftKeyboard()
        }
        select("Dissolution")
        scrollToText("Particle model of dissolution")
        compose.onNodeWithText("Particle model of dissolution").assertIsDisplayed()
        select("Ideal gas")
        fill("Amount (mol)","1"); fill("Absolute temperature (K)","300"); fill("Volume (L)","1")
        scrollToText("P = 2494.338785 kPa")
        compose.onNodeWithText("P = 2494.338785 kPa").assertIsDisplayed()
        select("Acidity")
        fill("Acid concentration (mol/L)","0")
        scrollToText("pH = 7")
        compose.onNodeWithText("pH = 7").assertIsDisplayed()
        select("Molecules 3D")
        scrollToText("3D molecular geometry")
        compose.onNodeWithText("3D molecular geometry").assertIsDisplayed()
        compose.onNodeWithText("CH₄").performScrollTo().performClick()
        compose.onNodeWithText("Rotate 30°").performScrollTo().performClick()
        compose.onNodeWithText("Reset view").performScrollTo().performClick()
        select("Balance")
        fill("Neutral equation", "Fe + O2 -> Fe2O3")
        compose.onNodeWithText("Balance equation").performScrollTo().performClick()
        scrollToText("4 Fe + 3 O2 → 2 Fe2O3")
        compose.onNodeWithText("4 Fe + 3 O2 → 2 Fe2O3").assertIsDisplayed()
        compose.onNodeWithText("Home").performClick()
        scrollToText("Gold and minerals")
        compose.onNodeWithText("Gold and minerals").performClick()
        compose.onNodeWithText("Contained-gold calculator").performClick()
        fill("Ore mass (metric tonnes)","10"); fill("Grade (g/t)","2"); fill("Hypothetical recovery (%)","60")
        val gold="Theoretical contained gold: 20 g\nHypothetically recovered: 12 g"
        scrollToText(gold)
        compose.onNodeWithText(gold).assertIsDisplayed()
        fill("Hypothetical recovery (%)","101")
        scrollToText("Invalid values. Check the limits and units above.")
        compose.onNodeWithText("Invalid values. Check the limits and units above.").assertIsDisplayed()
        compose.onNodeWithText("Settings").performClick()
        scrollToText("Dark")
        compose.onNodeWithText("Dark").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Reduce motion").performScrollTo().performClick()
        scenario.recreate()
        compose.waitUntil(60_000) { compose.onAllNodesWithText("Chemistry Lab").fetchSemanticsNodes().isNotEmpty() }
        scrollToText("Dark")
        compose.onNodeWithText("Dark").assertIsSelected()
        compose.onNodeWithContentDescription("Reduce motion").assertIsOn()
    }

}
