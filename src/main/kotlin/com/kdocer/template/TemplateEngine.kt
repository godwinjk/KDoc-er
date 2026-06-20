package com.kdocer.template

/**
 * Resolves `{placeholder}` tokens in a template string against a context map.
 *
 * Unknown tokens render as empty. After substitution, a dangling trailing article
 * (e.g. "the"/"a"/"an" left behind by an empty `{noun}`) and any doubled whitespace
 * are stripped so lines such as `* @param x the {noun}` collapse cleanly to `* @param x`.
 */
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
