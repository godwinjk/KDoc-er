package com.kdocer.aspect

import org.jetbrains.kotlin.psi.KtElement

/**
 * A pluggable framework/pattern detector. Each aspect inspects a declaration and, when it
 * matches a known pattern (a `@Composable`, a `ViewModel`, a `data class`, …), contributes
 * extra documentation. New framework support is added simply by registering another
 * implementation in [AspectEngine.ASPECTS] — no generator changes required.
 */
interface KDocAspect {

    /** Stable id used as the key for overriding [defaultNote] via the style sheet. */
    val id: String

    /** Default note text. May contain the `{name}` token (the humanised element name). */
    val defaultNote: String

    /** Whether this aspect applies to [element]. */
    fun matches(element: KtElement): Boolean

    /** Extra KDoc tag lines (without the leading `* `), e.g. `@see X`. Usually empty. */
    fun tags(element: KtElement): List<String> = emptyList()
}

/**
 * The merged result of every matching aspect for one element.
 *
 * @property notes description-level lines (without the leading `* `) inserted after the body
 * @property tags KDoc tag lines (without the leading `* `) appended before the closing `*​/`
 */
data class AspectContribution(
    val notes: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
) {
    val isEmpty: Boolean get() = notes.isEmpty() && tags.isEmpty()

    companion object {
        val EMPTY = AspectContribution()
    }
}
