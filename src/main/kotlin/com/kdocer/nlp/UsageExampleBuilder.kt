package com.kdocer.nlp

/**
 * Builds a small fenced usage example for a declaration, e.g. for `getUser(): User`:
 *
 * ```
 * val user = getUser()
 * ```
 *
 * Variable names are derived from the function name and return type:
 * - Boolean returns: `is` + noun from function name (e.g. `placeOrder` → `isPlacedOrder`)
 * - Numeric/String returns: noun extracted from the function name (e.g. `calculateTotal` → `total`)
 * - Other types: lowercased type name (e.g. `User` → `user`)
 * - Fallback: `result`
 */
object UsageExampleBuilder {

    private val PRIMITIVE_TYPES = setOf("Boolean", "Int", "Long", "Double", "Float", "Short", "Byte", "String", "Char")

    private val BOOLEAN_VERB_PREFIXES = setOf(
        "is", "has", "can", "should", "was", "will", "must", "are",
        "check", "contains", "equals", "matches", "supports", "allows", "validate", "verify",
    )

    private val PAST_TENSE_MAP = mapOf(
        "place" to "placed", "create" to "created", "update" to "updated",
        "delete" to "deleted", "remove" to "removed", "send" to "sent",
        "add" to "added", "save" to "saved", "set" to "set",
        "get" to "got", "find" to "found", "make" to "made",
        "run" to "run", "do" to "done", "process" to "processed",
        "execute" to "executed", "start" to "started", "stop" to "stopped",
        "close" to "closed", "open" to "opened", "enable" to "enabled",
        "disable" to "disabled", "connect" to "connected", "load" to "loaded",
        "submit" to "submitted", "complete" to "completed", "finish" to "finished",
        "handle" to "handled", "apply" to "applied", "register" to "registered",
        "cancel" to "cancelled", "accept" to "accepted", "reject" to "rejected",
        "confirm" to "confirmed", "publish" to "published", "insert" to "inserted",
        "fetch" to "fetched", "compute" to "computed", "calculate" to "calculated",
    )

    private val VERB_TO_NOUN_SUFFIX = mapOf(
        "summarize" to "summary", "summarise" to "summary",
        "serialize" to "serialized", "serialise" to "serialised",
        "convert" to "converted", "transform" to "transformed",
        "parse" to "parsed", "format" to "formatted",
        "encode" to "encoded", "decode" to "decoded",
        "compress" to "compressed", "decompress" to "decompressed",
    )

    fun forFunction(
        name: String,
        paramNames: List<String>,
        returnTypeText: String?,
        receiverTypeText: String?,
    ): List<String> {
        if (name.isBlank()) return emptyList()

        val args = paramNames.filter { it.isNotBlank() }.joinToString(", ")
        val receiver = receiverTypeText?.let { "${typeToVarName(it)}." } ?: ""
        val call = "$receiver$name($args)"

        val line =
            if (returnTypeText != null && returnTypeText != "Unit") {
                val varName = deriveVarName(name, returnTypeText)
                "val $varName = $call"
            } else call

        return listOf("```", line, "```")
    }

    private fun deriveVarName(functionName: String, returnType: String): String {
        val baseType = returnType.substringBefore('<').substringAfterLast('.').trim().trimEnd('?')
        val words = WordSplitter.split(functionName)
        if (words.isEmpty()) return "result"

        val verb = words.first()
        val nounWords = words.drop(1)

        return when {
            baseType == "Boolean" -> deriveBooleanVarName(verb, nounWords, functionName)
            PRIMITIVE_TYPES.contains(baseType) || baseType in setOf("Number", "BigDecimal", "BigInteger") ->
                derivePrimitiveVarName(verb, nounWords, functionName)
            else -> typeToVarName(returnType)
        }
    }

    private fun deriveBooleanVarName(verb: String, nounWords: List<String>, functionName: String): String {
        if (verb in BOOLEAN_VERB_PREFIXES) {
            return toCamelCase(WordSplitter.split(functionName))
        }

        val nounPart = if (nounWords.isNotEmpty()) nounWords.joinToString("") { it.replaceFirstChar { c -> c.uppercase() } } else ""
        val pastVerb = pastTense(verb)
        return if (nounPart.isNotEmpty()) {
            "is${pastVerb.replaceFirstChar { it.uppercase() }}$nounPart"
        } else {
            "is${pastVerb.replaceFirstChar { it.uppercase() }}"
        }
    }

    private fun derivePrimitiveVarName(verb: String, nounWords: List<String>, functionName: String): String {
        VERB_TO_NOUN_SUFFIX[verb]?.let { return it }

        if (nounWords.isNotEmpty()) {
            return toCamelCase(nounWords)
        }

        val words = WordSplitter.split(functionName)
        if (words.size == 1) {
            return "result"
        }
        return toCamelCase(words.drop(1))
    }

    private fun pastTense(verb: String): String {
        PAST_TENSE_MAP[verb]?.let { return it }
        return when {
            verb.endsWith("e") -> "${verb}d"
            verb.endsWith("y") && verb.length > 1 && verb[verb.length - 2] !in "aeiou" ->
                "${verb.dropLast(1)}ied"
            verb.last() in "bdfgklmnprstv" && verb.length > 2 &&
                verb[verb.length - 1] !in "aeiou" && verb[verb.length - 2] in "aeiou" ->
                "${verb}${verb.last()}ed"
            else -> "${verb}ed"
        }
    }

    private fun toCamelCase(words: List<String>): String {
        if (words.isEmpty()) return "result"
        return words.first() + words.drop(1).joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    private fun typeToVarName(typeText: String): String {
        val simple = typeText.substringBefore('<').substringAfterLast('.').trim().trimEnd('?')
        val ident = simple.takeWhile { it.isLetterOrDigit() || it == '_' }
        return if (ident.isEmpty()) "result" else ident.replaceFirstChar { it.lowercase() }
    }
}
