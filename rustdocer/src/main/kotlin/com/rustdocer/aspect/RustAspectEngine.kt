package com.rustdocer.aspect

import com.intellij.psi.PsiElement
import com.docer.engine.nlp.WordSplitter
import com.docer.engine.style.ResolvedStyle
import com.docer.engine.template.TemplateEngine
import org.rust.lang.core.psi.ext.RsNamedElement

/**
 * Runs every registered [RustAspect] against an element and merges their contributions.
 *
 * The registry is the single extension point for crate awareness. Note text can be overridden
 * per project via `aspects.notes.<id>` in `.rustdocer.yaml`; the `{name}` token in a note is
 * filled with the humanised element name.
 */
object RustAspectEngine {

    /** Order here is the order notes appear in the generated RustDoc. */
    val ASPECTS: List<RustAspect> = listOf(
        DeriveAspect,
        SerdeAspect,
        AsyncTraitAspect,
        BuilderAspect,
        ErrorAspect,
        UnsafeAspect,
    )

    fun analyze(element: PsiElement, style: ResolvedStyle): AspectContribution {
        if (!style.frameworkAware) return AspectContribution.EMPTY

        val name = (element as? RsNamedElement)?.name.orEmpty()
        val nameCtx = mapOf("name" to WordSplitter.humanize(name))

        val notes = mutableListOf<String>()
        val tags = mutableListOf<String>()
        for (aspect in ASPECTS) {
            if (!aspect.matches(element)) continue
            // Special handling for derive aspect to include trait list
            val text = if (aspect is DeriveAspect) {
                val deriveNote = DeriveAspect.deriveNote(element)
                if (deriveNote.isNotEmpty()) deriveNote else continue
            } else {
                val override = style.aspectNotes[aspect.id]
                if (override != null) TemplateEngine.render(override, nameCtx) else aspect.defaultNote
            }
            notes += text
            tags += aspect.tags(element)
        }
        return AspectContribution(notes, tags)
    }
}
