package com.dartdocer.generator

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.dartdocer.aspect.DartAspectEngine
import com.docer.engine.nlp.PhraseBuilder
import com.docer.engine.style.ResolvedStyle
import com.docer.engine.template.TemplateEngine
import com.dartdocer.style.DartStyleLoader
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.psi.DartGetterDeclaration
import com.jetbrains.lang.dart.psi.DartVarDeclarationList

/**
 * Generates DartDoc for fields, getters, and variable declarations.
 */
internal class DartFieldDocGenerator(
    private val project: Project,
    private val element: PsiElement,
) : DartDocGenerator {

    override fun generate(): String {
        val style = DartStyleLoader.resolve(project)
        val aspects = DartAspectEngine.analyze(element, style)

        // Nothing to append and no framework note to add: emit a minimal stub.
        if (!style.appendName && aspects.isEmpty) return "///\n"

        val builder = StringBuilder()

        // Description line.
        if (style.appendName) {
            val description = describe(style)
            builder.appendDocLine(
                TemplateEngine.render(
                    style.template.propertyDescription,
                    mapOf("description" to description),
                )
            )
        } else {
            builder.appendDocLine()
        }

        // Framework-aware notes.
        if (aspects.notes.isNotEmpty()) {
            builder.appendEmptyDocLine()
            aspects.notes.forEach { builder.appendDocLine(it) }
        }

        // Aspect tag lines.
        aspects.tags.forEach { builder.appendDocLine(it) }

        // @since version stamp.
        if (style.sinceTag && style.sinceVersion.isNotBlank()) {
            builder.appendEmptyDocLine()
            builder.appendDocLine("Since ${style.sinceVersion}.")
        }

        return builder.toString()
    }

    private fun describe(style: ResolvedStyle): String {
        val rawName = getName() ?: "Field"
        return if (style.splitNames) {
            PhraseBuilder.describe(
                rawName,
                returnTypeText = getTypeText(),
                verbMapping = style.verbMapping,
                collectionTypes = PhraseBuilder.DART_COLLECTION_TYPES,
            )
        } else {
            rawName
        }
    }

    private fun getName(): String? = when (element) {
        is DartGetterDeclaration -> element.name
        is DartVarDeclarationList -> element.varAccessDeclaration?.componentName?.text
        is DartComponent -> element.name
        else -> null
    }

    private fun getTypeText(): String? = when (element) {
        is DartGetterDeclaration -> element.returnType?.text
        is DartVarDeclarationList -> element.varAccessDeclaration?.type?.text
        else -> null
    }
}
