package com.docer.engine.nlp

object PhraseBuilder {

    val KOTLIN_COLLECTION_TYPES =
        setOf("List", "Set", "Collection", "Iterable", "Sequence", "Array", "Map", "Flow")

    val DART_COLLECTION_TYPES =
        setOf("List", "Set", "Map", "Iterable", "Queue", "Stream")

    val RUST_COLLECTION_TYPES =
        setOf("Vec", "HashMap", "HashSet", "BTreeMap", "BTreeSet", "VecDeque", "LinkedList", "Iterator", "Stream")

    private val TRAILING_ARTICLE = Regex("\\s+(the|a|an)\\s*$", RegexOption.IGNORE_CASE)

    fun describe(
        name: String?,
        returnTypeText: String? = null,
        receiverTypeText: String? = null,
        verbMapping: Map<String, String> = emptyMap(),
        collectionTypes: Set<String> = KOTLIN_COLLECTION_TYPES,
    ): String {
        if (name.isNullOrBlank()) return ""
        val words = WordSplitter.split(name)
        if (words.isEmpty()) return ""

        val resolved = VerbMapper.resolve(words, verbMapping)
        val sentence = if (resolved != null) {
            val (phrase, nounWords) = resolved
            phrase.replace("{noun}", noun(nounWords, returnTypeText, collectionTypes)).cleanup()
        } else {
            words.joinToString(" ")
        }

        val withReceiver =
            if (!receiverTypeText.isNullOrBlank()) "$sentence on the receiver [$receiverTypeText]" else sentence

        return withReceiver.replaceFirstChar { it.uppercase() }
    }

    private fun noun(nounWords: List<String>, returnTypeText: String?, collectionTypes: Set<String>): String {
        if (nounWords.isEmpty()) return ""
        return if (returnTypeText != null && isCollection(returnTypeText, collectionTypes)) {
            (nounWords.dropLast(1) + Pluralizer.pluralize(nounWords.last())).joinToString(" ")
        } else {
            nounWords.joinToString(" ")
        }
    }

    private fun isCollection(typeText: String, collectionTypes: Set<String>): Boolean {
        val outer = typeText.trim().substringBefore('<').substringAfterLast('.')
        return outer in collectionTypes
    }

    private fun String.cleanup(): String =
        replace(TRAILING_ARTICLE, "").replace(Regex("\\s{2,}"), " ").trim()

    fun returnPhrase(
        returnTypeText: String?,
        collectionTypes: Set<String> = KOTLIN_COLLECTION_TYPES,
        unitTypes: Set<String> = setOf("Unit", "Nothing"),
    ): String? {
        if (returnTypeText.isNullOrBlank()) return null
        val type = returnTypeText.trim().trimEnd('?')
        val outer = type.substringBefore('<').substringAfterLast('.').trim()
        return when {
            outer.isEmpty() || outer in unitTypes -> null
            outer == "Boolean" || outer == "bool" -> "`true` if the condition holds, `false` otherwise"
            isCollection(type, collectionTypes) && outer != "Map" && outer != "HashMap" -> {
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
