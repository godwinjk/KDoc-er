package com.rustdocer.generator

import com.intellij.openapi.project.Project
import com.docer.engine.nlp.PhraseBuilder
import com.docer.engine.template.TemplateEngine
import com.rustdocer.aspect.RustAspectEngine
import com.rustdocer.nlp.RustSeeReferenceResolver
import com.rustdocer.style.RustStyleLoader
import org.rust.lang.core.psi.RsImplItem

class RustImplDocGenerator(private val project: Project, private val element: RsImplItem) :
    RustDocGenerator {

    override fun generate(): String {
        val style = RustStyleLoader.resolve(project)
        val aspects = RustAspectEngine.analyze(element, style)

        val builder = StringBuilder()

        // Description line.
        val typeRef = element.typeReference?.text ?: "Type"
        val traitRef = element.traitRef?.path?.text

        val description = if (traitRef != null) {
            "Implementation of `$traitRef` for `$typeRef`."
        } else {
            "Inherent implementation for `$typeRef`."
        }
        builder.appendDocLine(description)

        // Framework-aware notes.
        aspects.notes.forEach { builder.appendDocLine(it) }

        // See references.
        if (style.seeReferences) {
            val refs = RustSeeReferenceResolver.resolve(element)
            if (refs.isNotEmpty()) {
                builder.appendEmptyDocLine()
                refs.forEach { builder.appendDocLine("See also: [`$it`]") }
            }
        }

        // Framework-aware tag lines.
        aspects.tags.forEach { builder.appendDocLine(it) }

        // Since tag.
        if (style.sinceTag && style.sinceVersion.isNotBlank()) {
            builder.appendEmptyDocLine()
            builder.appendDocLine("Since: ${style.sinceVersion}")
        }

        return builder.toString()
    }
}
