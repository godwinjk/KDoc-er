package com.kdocer.action

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Verifies the KDoc removal actions: single removal at the caret and whole-file removal.
 */
class RemoveActionTest : BasePlatformTestCase() {

    private val withDocs = """
        /**
         * A class.
         */
        class Sample {
            /**
             * A function.
             */
            fun run() {}

            /**
             * A property.
             */
            val count: Int = 0
        }
    """.trimIndent()

    fun testRemoveAllClearsEveryKDoc() {
        myFixture.configureByText("a.kt", withDocs)
        myFixture.testAction(KDocerAllRemoveAction())
        assertFalse(myFixture.editor.document.text, myFixture.editor.document.text.contains("/**"))
    }

    fun testRemoveSingleAtCaretOnlyClearsThatDeclaration() {
        // Caret on the function name -> only its KDoc is removed.
        val src = withDocs.replace("fun run()", "fun ru<caret>n()")
        myFixture.configureByText("a.kt", src)
        myFixture.testAction(KDocerRemoveAction())

        val text = myFixture.editor.document.text
        assertFalse(text, text.contains("A function."))
        assertTrue(text, text.contains("A class."))
        assertTrue(text, text.contains("A property."))
    }
}
