package com.docer.engine.merge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DocMergerTest {

    private val existing = """
        /**
         * My carefully written summary.
         *
         * @param id the unique id
         * @throws IllegalStateException when broken
         */
    """.trimIndent()

    private val generated = """
        /**
         * Saves the user
         *
         * @param id the id
         * @param name the name
         * @return the result
         */
    """.trimIndent()

    @Test
    fun mergeKeepsUserSummaryAndTagTextAndAddsMissing() {
        val merged = DocMerger.merge(existing, generated)

        assertTrue(merged, merged.contains("My carefully written summary."))
        assertTrue(merged, !merged.contains("Saves the user"))
        assertTrue(merged, merged.contains("@param id the unique id"))
        assertTrue(merged, !merged.contains("@param id the id"))
        assertTrue(merged, merged.contains("@param name the name"))
        assertTrue(merged, merged.contains("@return the result"))
        assertTrue(merged, merged.contains("@throws IllegalStateException when broken"))
    }

    @Test
    fun mergeUsesGeneratedSummaryWhenExistingHasNone() {
        val noSummary = "/**\n * @param id the unique id\n */"
        val merged = DocMerger.merge(noSummary, generated)
        assertTrue(merged, merged.contains("Saves the user"))
    }

    @Test
    fun resolveKeepReturnsNull() {
        assertNull(DocMerger.resolve(existing, generated, ExistingDocPolicy.KEEP))
    }

    @Test
    fun resolveReplaceReturnsGenerated() {
        assertEquals(generated, DocMerger.resolve(existing, generated, ExistingDocPolicy.REPLACE))
    }

    @Test
    fun resolveWithNoExistingReturnsGenerated() {
        assertEquals(generated, DocMerger.resolve(null, generated, ExistingDocPolicy.MERGE))
    }

    @Test
    fun tripleSlashFormatMergesCorrectly() {
        val existingTriple = "/// My summary.\n/// \n/// More detail.\n"
        val generatedTriple = "/// Generated summary.\n/// \n/// Extra info.\n"
        val merged = DocMerger.merge(existingTriple, generatedTriple, DocCommentFormat.TRIPLE_SLASH)
        assertTrue(merged, merged.contains("My summary."))
        assertTrue(merged, !merged.contains("Generated summary."))
    }
}
