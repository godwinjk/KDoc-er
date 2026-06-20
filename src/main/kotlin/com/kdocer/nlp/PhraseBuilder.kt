package com.kdocer.nlp

/**
 * Builds a human-readable description sentence for a declaration from its name and
 * (optionally) its type information. This is the rule-based "natural comment" engine:
 * it combines [WordSplitter], [VerbMapper] and [Pluralizer] rather than emitting a
 * fixed string per element.
 */
object PhraseBuilder {

    private val COLLECTION_TYPES =
        setOf("List", "Set", "Collection", "Iterable", "Sequence", "Array", "Map", "Flow")

    private val TRAILING_ARTICLE = Regex("\\s+(the|a|an)\\s*$", RegexOption.IGNORE_CASE)

    /**
     * @param name the declaration name (function/property/class)
     * @param returnTypeText the rendered return/property type, used for pluralisation
     * @param receiverTypeText the extension receiver type, embedded into the phrase when present
     * @param verbMapping custom verb -> phrase overrides from the style sheet
     */
    fun describe(
        name: String?,
        returnTypeText: String? = null,
        receiverTypeText: String? = null,
        verbMapping: Map<String, String> = emptyMap(),
    ): String {
        if (name.isNullOrBlank()) return ""
        val words = WordSplitter.split(name)
        if (words.isEmpty()) return ""

        val resolved = VerbMapper.resolve(words, verbMapping)
        val sentence = if (resolved != null) {
            val (phrase, nounWords) = resolved
            phrase.replace("{noun}", noun(nounWords, returnTypeText)).cleanup()
        } else {
            // No verb: the name itself is the subject (typical for classes/properties).
            words.joinToString(" ")
        }

        val withReceiver =
            if (!receiverTypeText.isNullOrBlank()) "$sentence on the receiver [$receiverTypeText]" else sentence

        return withReceiver.replaceFirstChar { it.uppercase() }
    }

    private fun noun(nounWords: List<String>, returnTypeText: String?): String {
        if (nounWords.isEmpty()) return ""
        return if (returnTypeText != null && isCollection(returnTypeText)) {
            (nounWords.dropLast(1) + Pluralizer.pluralize(nounWords.last())).joinToString(" ")
        } else {
            nounWords.joinToString(" ")
        }
    }

    private fun isCollection(typeText: String): Boolean {
        val outer = typeText.trim().substringBefore('<').substringAfterLast('.')
        return outer in COLLECTION_TYPES
    }

    private fun String.cleanup(): String =
        replace(TRAILING_ARTICLE, "").replace(Regex("\\s{2,}"), " ").trim()

    /**
     * A short `@return` description derived from the return type, e.g. `User` -> "the user",
     * `List<User>` -> "the users", `Boolean` -> a true/false clause. Returns `null` for
     * `Unit`/unknown so the caller can omit `@return`.
     */
    fun returnPhrase(returnTypeText: String?): String? {
        if (returnTypeText.isNullOrBlank()) return null
        val type = returnTypeText.trim().trimEnd('?')
        val outer = type.substringBefore('<').substringAfterLast('.').trim()
        return when {
            outer.isEmpty() || outer == "Unit" || outer == "Nothing" -> null
            outer == "Boolean" -> "`true` if the condition holds, `false` otherwise"
            isCollection(type) && outer != "Map" -> {
                val inner = type.substringAfter('<', "").substringBefore(',')
                    .substringBeforeLast('>').substringAfterLast('.').trim()
                val words = WordSplitter.split(inner).ifEmpty { WordSplitter.split(outer) }
                if (words.isEmpty()) null
                else "the " + (words.dropLast(1) + Pluralizer.pluralize(words.last())).joinToString(" ")
            }
            else -> {
                val words = WordSplitter.split(outer)
                if (words.isEmpty()) null else "the " + words.joinToString(" ")
            }
        }
    }
}
