package com.kdocer.merge

/**
 * Non-destructively combines an existing KDoc with a freshly generated one.
 *
 * The merge keeps the user's summary (when present) and the exact text of any tag they
 * already wrote, appends only the tags the generator produced that are missing, and never
 * drops tags the user added by hand (e.g. `@throws`, `@sample`). It works purely on KDoc
 * text — no PSI or project needed — so it is fully unit-testable.
 */
object KDocMerger {

    /** Tags whose first token after the name is a subject (e.g. the parameter name). */
    private val SUBJECT_TAGS = setOf("param", "property", "throws", "exception")

    private data class Tag(val name: String, val subject: String?, val lines: List<String>)
    private data class Doc(val summary: List<String>, val tags: List<Tag>)

    /**
     * Resolves the text to insert given the current [policy].
     *
     * @return the KDoc text to write, or `null` to leave an existing KDoc untouched.
     */
    fun resolve(existingText: String?, generated: String, policy: ExistingKDocPolicy): String? = when {
        existingText == null -> generated
        policy == ExistingKDocPolicy.REPLACE -> generated
        policy == ExistingKDocPolicy.KEEP -> null
        else -> merge(existingText, generated)
    }

    fun merge(existing: String, generated: String): String {
        val old = parse(existing)
        val new = parse(generated)

        val summary = old.summary.ifEmpty { new.summary }

        val mergedTags = ArrayList(old.tags)
        new.tags.forEach { generatedTag ->
            val alreadyPresent = old.tags.any { it.name == generatedTag.name && it.subject == generatedTag.subject }
            if (!alreadyPresent) mergedTags.add(generatedTag)
        }

        return render(summary, mergedTags)
    }

    private fun parse(doc: String): Doc {
        val content = doc.lines().map { strip(it) }

        val summary = ArrayList<String>()
        val tags = ArrayList<Tag>()
        var current: ArrayList<String>? = null

        for (line in content) {
            if (line.startsWith("@")) {
                current = arrayListOf(line)
                tags += toTag(current)
            } else if (current != null) {
                if (line.isNotEmpty()) {
                    current.add(line)
                    // Replace the last tag with the extended line list.
                    tags[tags.lastIndex] = toTag(current)
                }
            } else if (line.isNotEmpty() || summary.isNotEmpty()) {
                summary.add(line)
            }
        }
        return Doc(summary.dropLastWhile { it.isEmpty() }, tags)
    }

    /** Strips the comment scaffolding (`/**`, `*/`, leading `*`) from a raw line. */
    private fun strip(raw: String): String {
        var s = raw.trim()
        if (s.startsWith("/**")) s = s.removePrefix("/**").trim()
        if (s.endsWith("*/")) s = s.removeSuffix("*/").trim()
        if (s.startsWith("*")) s = s.removePrefix("*").trim()
        return s
    }

    private fun toTag(lines: List<String>): Tag {
        val head = lines.first().removePrefix("@").trim()
        val name = head.takeWhile { !it.isWhitespace() }
        val rest = head.drop(name.length).trim()
        val subject = if (name in SUBJECT_TAGS) rest.takeWhile { !it.isWhitespace() }.ifEmpty { null } else null
        return Tag(name, subject, lines)
    }

    private fun render(summary: List<String>, tags: List<Tag>): String {
        val sb = StringBuilder("/**\n")
        if (summary.isEmpty() && tags.isEmpty()) {
            return "/**\n * \n */\n"
        }
        summary.forEach { sb.append(" * ").append(it).append('\n') }
        if (summary.isNotEmpty() && tags.isNotEmpty()) sb.append(" *\n")
        tags.forEach { tag -> tag.lines.forEach { sb.append(" * ").append(it).append('\n') } }
        sb.append(" */\n")
        return sb.toString()
    }
}
