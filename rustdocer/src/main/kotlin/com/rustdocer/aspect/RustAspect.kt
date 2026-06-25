package com.rustdocer.aspect

import com.intellij.psi.PsiElement

/**
 * A pluggable crate/pattern detector for Rust. Each aspect inspects a declaration and,
 * when it matches a known pattern (a `#[derive(...)]`, serde attributes, `#[async_trait]`, ...),
 * contributes extra documentation. New crate support is added by registering another
 * implementation in [RustAspectEngine.ASPECTS].
 */
interface RustAspect {

    /** Stable id used as the key for overriding [defaultNote] via the style sheet. */
    val id: String

    /** Default note text. May contain the `{name}` token (the humanised element name). */
    val defaultNote: String

    /** Whether this aspect applies to [element]. */
    fun matches(element: PsiElement): Boolean

    /** Extra doc tag lines (without the leading `/// `), e.g. cross-references. Usually empty. */
    fun tags(element: PsiElement): List<String> = emptyList()
}

/**
 * The merged result of every matching aspect for one element.
 *
 * @property notes description-level lines (without the leading `/// `) inserted after the body
 * @property tags extra lines appended before the end
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
