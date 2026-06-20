package com.kdocer.nlp

/**
 * Minimal English pluralisation good enough for documentation phrases.
 * Not exhaustive — covers the common regular cases plus a few irregulars.
 */
object Pluralizer {

    private val IRREGULAR = mapOf(
        "child" to "children", "person" to "people", "man" to "men",
        "woman" to "women", "foot" to "feet", "tooth" to "teeth",
        "datum" to "data", "index" to "indices", "vertex" to "vertices",
    )

    fun pluralize(word: String): String {
        if (word.isBlank()) return word
        val lower = word.lowercase()
        IRREGULAR[lower]?.let { return it }
        return when {
            lower.endsWith("s") || lower.endsWith("x") || lower.endsWith("z") ||
                lower.endsWith("ch") || lower.endsWith("sh") -> word + "es"
            lower.endsWith("y") && word.length > 1 && word[word.length - 2] !in "aeiou" ->
                word.dropLast(1) + "ies"
            else -> word + "s"
        }
    }
}
