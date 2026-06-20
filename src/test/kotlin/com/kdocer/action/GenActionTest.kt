package com.kdocer.action

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.kdocer.service.KDocerSettings

/**
 * Verifies the KDoc generation actions: whole-file generation and single-declaration
 * generation at the caret.
 */
class GenActionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        with(KDocerSettings.getInstance()) {
            isAllowedClass = true
            isAllowedFun = true
            isAllowedPublic = true
            existingKDocPolicy = com.kdocer.merge.ExistingKDocPolicy.REPLACE
            isAppendName = true
        }
    }

    private fun kdocCount(text: String) = Regex("/\\*\\*").findAll(text).count()

    fun testGenAllDocumentsEveryDeclaration() {
        myFixture.configureByText("a.kt", "class Sample {\n    fun run() {}\n}")
        myFixture.testAction(KDocerAllGenAction())
        val text = myFixture.editor.document.text
        // One KDoc for the class, one for the function.
        assertEquals(text, 2, kdocCount(text))
    }

    fun testGenSingleAtCaretDocumentsOnlyThatDeclaration() {
        myFixture.configureByText("a.kt", "class Sample {\n    fun ru<caret>n() {}\n}")
        myFixture.testAction(KDocerGenAction())
        val text = myFixture.editor.document.text
        assertEquals(text, 1, kdocCount(text))
    }

    fun testMergePreservesUserTextAndAddsMissingTags() {
        KDocerSettings.getInstance().existingKDocPolicy = com.kdocer.merge.ExistingKDocPolicy.MERGE
        val src = """
            /**
             * Hand written summary.
             *
             * @param a the first one
             */
            fun ru<caret>n(a: Int, b: Int): Int = a + b
        """.trimIndent()
        myFixture.configureByText("a.kt", src)
        myFixture.testAction(KDocerGenAction())

        val text = myFixture.editor.document.text
        assertTrue(text, text.contains("Hand written summary."))   // user summary kept
        assertTrue(text, text.contains("@param a the first one"))   // user tag text kept
        assertTrue(text, text.contains("@param b"))                 // missing param added
        assertEquals(text, 1, kdocCount(text))                      // still one comment
    }
}
