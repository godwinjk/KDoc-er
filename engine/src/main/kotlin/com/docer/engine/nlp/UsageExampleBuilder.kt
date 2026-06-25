package com.docer.engine.nlp

object UsageExampleBuilder {

    val KOTLIN_PRIMITIVE_TYPES = setOf("Boolean", "Int", "Long", "Double", "Float", "Short", "Byte", "String", "Char")
    val DART_PRIMITIVE_TYPES = setOf("bool", "int", "double", "String", "num")
    val RUST_PRIMITIVE_TYPES = setOf("bool", "i8", "i16", "i32", "i64", "i128", "isize", "u8", "u16", "u32", "u64", "u128", "usize", "f32", "f64", "char", "String", "&str")

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

    enum class Language {
        KOTLIN, DART, RUST
    }

    fun forFunction(
        name: String,
        paramNames: List<String>,
        returnTypeText: String?,
        receiverTypeText: String?,
        language: Language = Language.KOTLIN,
    ): List<String> {
        if (name.isBlank()) return emptyList()

        val primitiveTypes = when (language) {
            Language.KOTLIN -> KOTLIN_PRIMITIVE_TYPES
            Language.DART -> DART_PRIMITIVE_TYPES
            Language.RUST -> RUST_PRIMITIVE_TYPES
        }

        val voidTypes = when (language) {
            Language.KOTLIN -> setOf("Unit")
            Language.DART -> setOf("void")
            Language.RUST -> setOf("()")
        }

        val args = paramNames.filter { it.isNotBlank() }.joinToString(", ")
        val receiver = receiverTypeText?.let { "${typeToVarName(it)}." } ?: ""
        val call = "$receiver$name($args)"

        val hasReturn = returnTypeText != null && returnTypeText !in voidTypes

        if (!hasReturn) return listOf("```", call, "```")

        val varName = deriveVarName(name, returnTypeText!!, primitiveTypes)
        val line = when (language) {
            Language.KOTLIN -> "val $varName = $call"
            Language.DART -> "final $varName = $call;"
            Language.RUST -> "let $varName = $call;"
        }

        return listOf("```", line, "```")
    }

    private fun deriveVarName(functionName: String, returnType: String, primitiveTypes: Set<String>): String {
        val baseType = returnType.substringBefore('<').substringAfterLast('.').trim().trimEnd('?')
        val words = WordSplitter.split(functionName)
        if (words.isEmpty()) return "result"

        val verb = words.first()
        val nounWords = words.drop(1)

        return when {
            baseType == "Boolean" || baseType == "bool" -> deriveBooleanVarName(verb, nounWords, functionName)
            primitiveTypes.contains(baseType) || baseType in setOf("Number", "BigDecimal", "BigInteger") ->
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
