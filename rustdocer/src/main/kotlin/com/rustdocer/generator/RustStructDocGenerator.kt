package com.rustdocer.generator

import com.intellij.openapi.project.Project
import com.docer.engine.nlp.PhraseBuilder
import com.docer.engine.nlp.WordSplitter
import com.docer.engine.style.ResolvedStyle
import com.docer.engine.template.TemplateEngine
import com.rustdocer.aspect.DeriveAspect
import com.rustdocer.aspect.RustAspectEngine
import com.rustdocer.nlp.RustSeeReferenceResolver
import com.rustdocer.style.RustStyleLoader
import org.rust.lang.core.psi.RsNamedFieldDecl
import org.rust.lang.core.psi.RsStructItem
import org.rust.lang.core.psi.ext.RsVisibility

class RustStructDocGenerator(private val project: Project, private val element: RsStructItem) :
    RustDocGenerator {

    override fun generate(): String {
        val style = RustStyleLoader.resolve(project)
        val t = style.template
        val aspects = RustAspectEngine.analyze(element, style)
        val rawName = element.name ?: "Struct"

        val builder = StringBuilder()

        // Description line.
        if (style.appendName) {
            val description =
                if (style.splitNames) PhraseBuilder.describe(rawName, verbMapping = style.verbMapping, collectionTypes = PhraseBuilder.RUST_COLLECTION_TYPES) else rawName
            builder.appendDocLine(TemplateEngine.render(t.classDescription, mapOf("description" to description)))
        } else {
            builder.appendDocLine()
        }

        // Framework-aware notes (derive macros, serde, builder pattern, ...).
        aspects.notes.forEach { builder.appendDocLine(it) }

        // # Fields section for public fields.
        val fields = element.blockFields?.namedFieldDeclList ?: emptyList()
        val publicFields = fields.filter { isPublicField(it) }
        if (publicFields.isNotEmpty()) {
            builder.appendEmptyDocLine()
            builder.appendDocLine("# Fields")
            builder.appendEmptyDocLine()
            publicFields.forEach { field ->
                val fieldName = field.name ?: ""
                val noun = WordSplitter.nounOrEmpty(fieldName)
                val rendered = TemplateEngine.render(
                    t.propertyTagLine,
                    mapOf("name" to fieldName, "noun" to noun),
                )
                builder.appendDocLine(rendered)
            }
        }

        // Type parameters.
        val typeParams = element.typeParameterList?.typeParameterList ?: emptyList()
        if (typeParams.isNotEmpty()) {
            builder.appendEmptyDocLine()
            builder.appendDocLine("# Type Parameters")
            builder.appendEmptyDocLine()
            typeParams.forEach { tp ->
                val name = tp.name ?: ""
                builder.appendDocLine(TemplateEngine.render(t.typeParamLine, mapOf("name" to name)))
            }
        }

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

    private fun isPublicField(field: RsNamedFieldDecl): Boolean {
        val vis = field.visibility
        return vis is RsVisibility.Public
    }
}
