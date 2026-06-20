package com.kdocer.generator

import com.intellij.openapi.project.Project
import com.kdocer.aspect.AspectEngine
import com.kdocer.nlp.PhraseBuilder
import com.kdocer.style.StyleLoader
import com.kdocer.template.TemplateEngine
import org.jetbrains.kotlin.psi.KtProperty

class PropertyKDocGenerator(private val project: Project, private val element: KtProperty) :
    KDocGenerator {

    override fun generate(): String {
        val style = StyleLoader.resolve(project)
        val aspects = AspectEngine.analyze(element, style)

        // Nothing to append and no framework note to add: emit a minimal stub.
        if (!style.appendName && aspects.isEmpty) return "/**\n *\n */\n"

        val builder = StringBuilder()
        builder.appendLine("/**")

        builder.append("* ")
        if (style.appendName) {
            builder.append(TemplateEngine.render(style.template.propertyDescription, mapOf("description" to describe(style))))
        }
        builder.appendLine()

        aspects.notes.forEach { builder.appendLine("* $it") }
        aspects.tags.forEach { builder.appendLine("* $it") }

        // @since version stamp.
        if (style.sinceTag && style.sinceVersion.isNotBlank()) {
            builder.appendLine("* @since ${style.sinceVersion}")
        }

        builder.appendLine("*/")
        return builder.toString()
    }

    private fun describe(style: com.kdocer.style.ResolvedStyle): String {
        val rawName = element.name ?: "Property"
        return if (style.splitNames) {
            PhraseBuilder.describe(rawName, returnTypeText = element.typeReference?.text, verbMapping = style.verbMapping)
        } else {
            rawName
        }
    }
}
