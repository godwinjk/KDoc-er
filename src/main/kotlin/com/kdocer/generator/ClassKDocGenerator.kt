package com.kdocer.generator

import com.intellij.openapi.project.Project
import com.kdocer.aspect.AspectEngine
import com.kdocer.nlp.PhraseBuilder
import com.kdocer.nlp.WordSplitter
import com.kdocer.style.StyleLoader
import com.kdocer.template.TemplateEngine
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Generates KDoc for classes and objects: a name-derived description, `@param` tags for
 * type parameters, `@property`/`@param` tags split from the primary constructor, and a
 * `@constructor` line.
 */
internal class ClassKDocGenerator(private val project: Project, private val element: KtClassOrObject) :
    KDocGenerator {

    override fun generate(): String {
        val style = StyleLoader.resolve(project)
        val t = style.template
        val aspects = AspectEngine.analyze(element, style)
        val rawName = element.name ?: "Class"

        val builder = StringBuilder()
        builder.appendLine("/**")

        builder.append("* ")
        if (style.appendName) {
            val description =
                if (style.splitNames) PhraseBuilder.describe(rawName, verbMapping = style.verbMapping) else rawName
            builder.append(TemplateEngine.render(t.classDescription, mapOf("description" to description)))
        }
        builder.appendLine()
        // Framework-aware notes (ViewModel, data class, sealed, object, util …).
        aspects.notes.forEach { builder.appendLine("* $it") }
        builder.appendLine("*")

        if (element.typeParameters.isNotEmpty()) {
            element.typeParameters.forEach {
                builder.appendLine(TemplateEngine.render(t.typeParamLine, mapOf("name" to (it.name ?: ""))))
            }
        }

        val isInterface = element is KtClass && element.isInterface()

        val (properties, parameters) = element.primaryConstructor?.valueParameters?.partition {
            it.hasValOrVar()
        } ?: Pair(emptyList(), emptyList())

        if (!isInterface && style.includeConstructor) {
            if (parameters.isNotEmpty()) {
                builder.appendLine(TemplateEngine.render(t.constructorLine, mapOf("description" to "")))
            } else {
                builder.appendLine(TemplateEngine.render(t.constructorEmptyLine, mapOf("name" to rawName)))
            }
        }

        properties.forEach { prop ->
            builder.appendLine(
                TemplateEngine.render(
                    t.propertyTagLine,
                    mapOf("name" to (prop.name ?: ""), "noun" to WordSplitter.nounOrEmpty(prop.name ?: "")),
                )
            )
        }

        // Interface properties are declared in the body, not the constructor.
        if (isInterface) {
            element.body?.declarations?.filterIsInstance<KtProperty>()?.forEach { prop ->
                builder.appendLine(
                    TemplateEngine.render(
                        t.propertyTagLine,
                        mapOf("name" to (prop.name ?: ""), "noun" to WordSplitter.nounOrEmpty(prop.name ?: "")),
                    )
                )
            }
        }

        if (parameters.isNotEmpty()) {
            parameters.forEach { param ->
                builder.appendLine(
                    TemplateEngine.render(
                        t.paramLine,
                        mapOf("name" to (param.name ?: ""), "noun" to WordSplitter.nounOrEmpty(param.name ?: "")),
                    )
                )
            }
        }

        aspects.tags.forEach { builder.appendLine("* $it") }
        builder.appendLine("*/")
        return builder.toString()
    }
}
