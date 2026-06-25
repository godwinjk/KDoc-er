package com.rustdocer.generator

import com.intellij.openapi.project.Project
import com.docer.engine.nlp.PhraseBuilder
import com.docer.engine.nlp.WordSplitter
import com.docer.engine.template.TemplateEngine
import com.rustdocer.aspect.RustAspectEngine
import com.rustdocer.nlp.RustSeeReferenceResolver
import com.rustdocer.style.RustStyleLoader
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsTraitItem
import org.rust.lang.core.psi.ext.block

class RustTraitDocGenerator(private val project: Project, private val element: RsTraitItem) :
    RustDocGenerator {

    override fun generate(): String {
        val style = RustStyleLoader.resolve(project)
        val t = style.template
        val aspects = RustAspectEngine.analyze(element, style)
        val rawName = element.name ?: "Trait"

        val builder = StringBuilder()

        // Description line.
        if (style.appendName) {
            val description =
                if (style.splitNames) PhraseBuilder.describe(rawName, verbMapping = style.verbMapping, collectionTypes = PhraseBuilder.RUST_COLLECTION_TYPES) else rawName
            builder.appendDocLine(TemplateEngine.render(t.classDescription, mapOf("description" to description)))
        } else {
            builder.appendDocLine()
        }

        // Framework-aware notes.
        aspects.notes.forEach { builder.appendDocLine(it) }

        // Required and provided methods.
        val members = element.members?.let { body ->
            body.children.filterIsInstance<RsFunction>()
        } ?: emptyList()

        val requiredMethods = members.filter { it.block == null }
        val providedMethods = members.filter { it.block != null }

        if (requiredMethods.isNotEmpty()) {
            builder.appendEmptyDocLine()
            builder.appendDocLine("# Required Methods")
            builder.appendEmptyDocLine()
            requiredMethods.forEach { method ->
                val methodName = method.name ?: ""
                val desc = WordSplitter.humanize(methodName)
                builder.appendDocLine("* [`$methodName`] - ${desc.replaceFirstChar { it.uppercase() }}.")
            }
        }

        if (providedMethods.isNotEmpty()) {
            builder.appendEmptyDocLine()
            builder.appendDocLine("# Provided Methods")
            builder.appendEmptyDocLine()
            providedMethods.forEach { method ->
                val methodName = method.name ?: ""
                val desc = WordSplitter.humanize(methodName)
                builder.appendDocLine("* [`$methodName`] - ${desc.replaceFirstChar { it.uppercase() }}.")
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
}
