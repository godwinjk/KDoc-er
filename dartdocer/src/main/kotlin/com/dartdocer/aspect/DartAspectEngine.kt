package com.dartdocer.aspect

import com.intellij.psi.PsiElement
import com.docer.engine.nlp.WordSplitter
import com.docer.engine.style.ResolvedStyle
import com.docer.engine.template.TemplateEngine
import com.jetbrains.lang.dart.psi.DartComponent

/**
 * Runs every registered [DartAspect] against an element and merges their contributions.
 *
 * The registry is the single extension point for framework awareness -- append a new
 * aspect here and it is picked up by all generators. Note text can be overridden per
 * project via `aspects.notes.<id>` in `.dartdocer.yaml`; the `{name}` token in a note is
 * filled with the humanised element name.
 */
object DartAspectEngine {

    /** Order here is the order notes appear in the generated DartDoc. */
    val ASPECTS: List<DartAspect> = listOf(
        FlutterStatelessWidgetAspect,
        FlutterStatefulWidgetAspect,
        FlutterProviderAspect,
        FreezedAspect,
        DartEnumAspect,
        DartMixinAspect,
        DartExtensionAspect,
        DartSingletonAspect,
    )

    fun analyze(element: PsiElement, style: ResolvedStyle): AspectContribution {
        if (!style.frameworkAware) return AspectContribution.EMPTY

        val name = (element as? DartComponent)?.name.orEmpty()
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
