package com.dartdocer.generator

import com.intellij.openapi.project.Project
import com.dartdocer.aspect.DartAspectEngine
import com.dartdocer.nlp.DartSeeReferenceResolver
import com.docer.engine.nlp.PhraseBuilder
import com.docer.engine.template.TemplateEngine
import com.dartdocer.style.DartStyleLoader
import com.jetbrains.lang.dart.psi.DartClassDefinition

/**
 * Generates DartDoc for classes: a name-derived description, constructor params documented
 * inline as `[paramName]` references, framework aspects, and see-also references.
 */
internal class DartClassDocGenerator(
    private val project: Project,
    private val element: DartClassDefinition,
) : DartDocGenerator {

    override fun generate(): String {
        val style = DartStyleLoader.resolve(project)
        val t = style.template
        val aspects = DartAspectEngine.analyze(element, style)
        val rawName = element.name ?: "Class"

        val builder = StringBuilder()

        // Description line.
        if (style.appendName) {
            val description = if (style.splitNames) {
                PhraseBuilder.describe(
                    rawName,
                    verbMapping = style.verbMapping,
                    collectionTypes = PhraseBuilder.DART_COLLECTION_TYPES,
                )
            } else rawName
            builder.appendDocLine(TemplateEngine.render(t.classDescription, mapOf("description" to description)))
        } else {
            builder.appendDocLine()
        }

        // Framework-aware notes (Flutter widgets, freezed, etc.).
        if (aspects.notes.isNotEmpty()) {
            builder.appendEmptyDocLine()
            aspects.notes.forEach { builder.appendDocLine(it) }
        }

        // Type parameters noted inline.
        val typeParams = element.typeParameters?.typeParameterList ?: emptyList()
        if (typeParams.isNotEmpty()) {
            builder.appendEmptyDocLine()
            val paramRefs = typeParams.mapNotNull { it.componentName?.text }.joinToString(", ") { "[$it]" }
            builder.appendDocLine("Type parameters: $paramRefs.")
        }

        // Constructor params documented inline as [paramName] references.
        val constructorParams = extractConstructorParams()
        if (constructorParams.isNotEmpty() && style.includeConstructor) {
            builder.appendEmptyDocLine()
            val paramRefs = constructorParams.joinToString(", ") { "[$it]" }
            builder.appendDocLine(
                TemplateEngine.render(
                    t.constructorLine,
                    mapOf("name" to rawName, "description" to "with $paramRefs"),
                )
            )
        } else if (style.includeConstructor) {
            builder.appendEmptyDocLine()
            builder.appendDocLine(TemplateEngine.render(t.constructorEmptyLine, mapOf("name" to rawName)))
        }

        // See references to superclass/mixins.
        if (style.seeReferences) {
            val refs = DartSeeReferenceResolver.resolve(element)
            if (refs.isNotEmpty()) {
                builder.appendEmptyDocLine()
                builder.appendDocLine("See also: ${refs.joinToString(", ") { "[$it]" }}.")
            }
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

    private fun extractConstructorParams(): List<String> {
        val params = mutableListOf<String>()
        val classMembers = element.classBody?.classMembers ?: return params

        // Look for method declarations that are constructors (name matches class name, or unnamed)
        classMembers.methodDeclarationList.forEach { method ->
            if (method.name == element.name || method.name == null) {
                // This is a constructor - extract its params
                val paramList = method.formalParameterList ?: return@forEach
                paramList.normalFormalParameterList.forEach { param ->
                    param.findComponentName()?.text?.let { name ->
                        params += name.removePrefix("_")
                    }
                }
                paramList.optionalFormalParameters
                    ?.defaultFormalNamedParameterList?.forEach { param ->
                        param.normalFormalParameter?.findComponentName()?.text?.let { name ->
                            params += name.removePrefix("_")
                        }
                    }
            }
        }
        return params
    }
}
