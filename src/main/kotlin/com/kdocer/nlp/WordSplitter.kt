package com.kdocer.nlp

/**
 * Splits a programming identifier into its constituent lowercase words.
 *
 * Handles camelCase, PascalCase, snake_case, kebab-case and ALL_CAPS, e.g.
 * `getUserId` -> [get, user, id], `HTTP_PROXY` -> [http, proxy].
 */
object WordSplitter {

    // Boundary between a lower/digit and an upper (fooBar), or between an
    // acronym run and a following word (HTTPServer -> HTTP, Server).
    private val CAMEL_BOUNDARY = Regex("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])")

    fun split(name: String): List<String> =
        name.split('_', ' ', '-', '.')
            .flatMap { it.split(CAMEL_BOUNDARY) }
            .filter { it.isNotBlank() }
            .map { it.lowercase() }

    /** Humanises an identifier into a space separated phrase, e.g. `userId` -> "user id". */
    fun humanize(name: String): String = split(name).joinToString(" ")

    /**
     * Humanised noun for a tag line, or an empty string when it would merely repeat
     * [name] verbatim (e.g. a parameter literally named `key` -> "" so the template
     * collapses `@param key the {noun}` to `@param key`).
     */
    fun nounOrEmpty(name: String): String {
        val noun = humanize(name)
        return if (noun.equals(name, ignoreCase = true)) "" else noun
    }
}
