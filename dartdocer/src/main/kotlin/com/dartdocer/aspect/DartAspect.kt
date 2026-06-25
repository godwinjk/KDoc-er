package com.dartdocer.aspect

import com.intellij.psi.PsiElement

/**
 * A pluggable framework/pattern detector for Dart. Each aspect inspects a declaration and,
 * when it matches a known pattern (a StatelessWidget, a @freezed class, a mixin, ...),
 * contributes extra documentation. New framework support is added simply by registering
 * another implementation in [DartAspectEngine.ASPECTS].
 */
interface DartAspect {

    /** Stable id used as the key for overriding [defaultNote] via the style sheet. */
    val id: String

    /** Default note text. May contain the `{name}` token (the humanised element name). */
    val defaultNote: String

    /** Whether this aspect applies to [element]. */
    fun matches(element: PsiElement): Boolean

    /** Extra doc lines (without the leading `/// `), e.g. "See also: [X]". Usually empty. */
    fun tags(element: PsiElement): List<String> = emptyList()
}

/**
 * The merged result of every matching aspect for one element.
 *
 * @property notes description-level lines (without the leading `/// `) inserted after the body
 * @property tags extra doc lines (without the leading `/// `) appended at the end
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
