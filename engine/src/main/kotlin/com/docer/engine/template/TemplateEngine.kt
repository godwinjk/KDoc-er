package com.docer.engine.template

object TemplateEngine {

    private val TOKEN = Regex("\\{(\\w+)}")
    private val TRAILING_ARTICLE = Regex("\\s+(the|a|an)\\s*$", RegexOption.IGNORE_CASE)
    private val DOUBLE_SPACE = Regex("[ \\t]{2,}")

    fun render(template: String, ctx: Map<String, String>): String =
        TOKEN.replace(template) { m -> ctx[m.groupValues[1]] ?: "" }
            .replace(TRAILING_ARTICLE, "")
            .replace(DOUBLE_SPACE, " ")
            .trimEnd()
}
