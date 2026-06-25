package com.kdocer.aspect

import com.docer.engine.nlp.WordSplitter
import com.docer.engine.style.ResolvedStyle
import com.docer.engine.template.TemplateEngine
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNamedDeclaration

/**
 * Runs every registered [KDocAspect] against an element and merges their contributions.
 *
 * The registry is the single extension point for framework awareness — append a new
 * aspect here and it is picked up by all generators. Note text can be overridden per
 * project via `aspects.notes.<id>` in `.kdocer.yaml`; the `{name}` token in a note is
 * filled with the humanised element name.
 */
object AspectEngine {

    /** Order here is the order notes appear in the generated KDoc. */
    val ASPECTS: List<KDocAspect> = listOf(
        ComposeAspect,
        ComposePreviewAspect,
        ViewModelAspect,
        DataClassAspect,
        SealedAspect,
        EnumAspect,
        CompanionAspect,
        SingletonObjectAspect,
        UtilAspect,
        LiveDataPropertyAspect,
        FlowStatePropertyAspect,
    )

    fun analyze(element: KtElement, style: ResolvedStyle): AspectContribution {
        if (!style.frameworkAware) return AspectContribution.EMPTY

        val name = (element as? KtNamedDeclaration)?.name.orEmpty()
        val nameCtx = mapOf("name" to WordSplitter.humanize(name))

        val notes = mutableListOf<String>()
        val tags = mutableListOf<String>()
        for (aspect in ASPECTS) {
            if (!aspect.matches(element)) continue
            val text = style.aspectNotes[aspect.id] ?: aspect.defaultNote
            notes += TemplateEngine.render(text, nameCtx)
            tags += aspect.tags(element)
        }
        return AspectContribution(notes, tags)
    }
}
