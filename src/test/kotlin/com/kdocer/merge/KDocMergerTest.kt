package com.kdocer.merge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure unit tests for the non-destructive KDoc merge logic.
 */
class KDocMergerTest {

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
        val merged = KDocMerger.merge(existing, generated)

        // User summary preserved (generated summary discarded).
        assertTrue(merged, merged.contains("My carefully written summary."))
        assertTrue(merged, !merged.contains("Saves the user"))

        // User's @param id text preserved, not overwritten by the generated one.
        assertTrue(merged, merged.contains("@param id the unique id"))
        assertTrue(merged, !merged.contains("@param id the id"))

        // Missing tags added.
        assertTrue(merged, merged.contains("@param name the name"))
        assertTrue(merged, merged.contains("@return the result"))

        // User-only tag never dropped.
        assertTrue(merged, merged.contains("@throws IllegalStateException when broken"))
    }

    @Test
    fun mergeUsesGeneratedSummaryWhenExistingHasNone() {
        val noSummary = "/**\n * @param id the unique id\n */"
        val merged = KDocMerger.merge(noSummary, generated)
        assertTrue(merged, merged.contains("Saves the user"))
    }

    @Test
    fun resolveKeepReturnsNull() {
        assertNull(KDocMerger.resolve(existing, generated, ExistingKDocPolicy.KEEP))
    }

    @Test
    fun resolveReplaceReturnsGenerated() {
        assertEquals(generated, KDocMerger.resolve(existing, generated, ExistingKDocPolicy.REPLACE))
    }

    @Test
    fun resolveWithNoExistingReturnsGenerated() {
        assertEquals(generated, KDocMerger.resolve(null, generated, ExistingKDocPolicy.MERGE))
    }
}
