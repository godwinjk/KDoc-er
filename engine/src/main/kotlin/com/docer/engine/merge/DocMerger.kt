package com.docer.engine.merge

object DocMerger {

    private data class Tag(val name: String, val subject: String?, val lines: List<String>)
    private data class Doc(val summary: List<String>, val tags: List<Tag>)

    fun resolve(existingText: String?, generated: String, policy: ExistingDocPolicy, format: DocCommentFormat = DocCommentFormat.KDOC): String? = when {
        existingText == null -> generated
        policy == ExistingDocPolicy.REPLACE -> generated
        policy == ExistingDocPolicy.KEEP -> null
        else -> merge(existingText, generated, format)
    }

    fun merge(existing: String, generated: String, format: DocCommentFormat = DocCommentFormat.KDOC): String {
        val old = parse(existing, format)
        val new = parse(generated, format)

        val summary = old.summary.ifEmpty { new.summary }

        val mergedTags = ArrayList(old.tags)
        new.tags.forEach { generatedTag ->
            val alreadyPresent = old.tags.any { it.name == generatedTag.name && it.subject == generatedTag.subject }
            if (!alreadyPresent) mergedTags.add(generatedTag)
        }

        return render(summary, mergedTags, format)
    }

    private fun parse(doc: String, format: DocCommentFormat): Doc {
        val content = doc.lines().map { strip(it, format) }

        val summary = ArrayList<String>()
        val tags = ArrayList<Tag>()
        var current: ArrayList<String>? = null

        for (line in content) {
            if (line.startsWith("@")) {
                current = arrayListOf(line)
                tags += toTag(current, format.subjectTags)
            } else if (current != null) {
                if (line.isNotEmpty()) {
                    current.add(line)
                    tags[tags.lastIndex] = toTag(current, format.subjectTags)
                }
            } else if (line.isNotEmpty() || summary.isNotEmpty()) {
                summary.add(line)
            }
        }
        return Doc(summary.dropLastWhile { it.isEmpty() }, tags)
    }

    private fun strip(raw: String, format: DocCommentFormat): String {
        var s = raw.trim()
        format.blockStart?.let { if (s.startsWith(it)) s = s.removePrefix(it).trim() }
        format.blockEnd?.let { if (s.endsWith(it.trim())) s = s.removeSuffix(it.trim()).trim() }
        val prefix = format.linePrefix.trim()
        if (prefix.isNotEmpty() && s.startsWith(prefix)) s = s.removePrefix(prefix).trim()
        return s
    }

    private fun toTag(lines: List<String>, subjectTags: Set<String>): Tag {
        val head = lines.first().removePrefix("@").trim()
        val name = head.takeWhile { !it.isWhitespace() }
        val rest = head.drop(name.length).trim()
        val subject = if (name in subjectTags) rest.takeWhile { !it.isWhitespace() }.ifEmpty { null } else null
        return Tag(name, subject, lines)
    }

    private fun render(summary: List<String>, tags: List<Tag>, format: DocCommentFormat): String {
        val prefix = format.linePrefix
        val sb = StringBuilder()

        if (format.blockStart != null) sb.append(format.blockStart).append('\n')

        if (summary.isEmpty() && tags.isEmpty()) {
            return if (format.blockStart != null) {
                "${format.blockStart}\n${prefix}\n${format.blockEnd}\n"
            } else {
                "${prefix}\n"
            }
        }

        summary.forEach { sb.append(prefix).append(it).append('\n') }
        if (summary.isNotEmpty() && tags.isNotEmpty()) sb.append(prefix.trimEnd()).append('\n')
        tags.forEach { tag -> tag.lines.forEach { sb.append(prefix).append(it).append('\n') } }

        if (format.blockEnd != null) sb.append(format.blockEnd).append('\n')

        return sb.toString()
    }
}
