package com.docer.engine.nlp

object WordSplitter {

    private val CAMEL_BOUNDARY = Regex("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])")

    fun split(name: String): List<String> =
        name.split('_', ' ', '-', '.')
            .flatMap { it.split(CAMEL_BOUNDARY) }
            .filter { it.isNotBlank() }
            .map { it.lowercase() }

    fun humanize(name: String): String = split(name).joinToString(" ")

    fun nounOrEmpty(name: String): String {
        val noun = humanize(name)
        return if (noun.equals(name, ignoreCase = true)) "" else noun
    }
}
