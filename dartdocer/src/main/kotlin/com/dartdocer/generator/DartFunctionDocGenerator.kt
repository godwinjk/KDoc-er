package com.dartdocer.generator

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.dartdocer.aspect.DartAspectEngine
import com.dartdocer.nlp.DartAsyncAnalyzer
import com.dartdocer.nlp.DartSeeReferenceResolver
import com.dartdocer.nlp.DartThrowsDetector
import com.docer.engine.nlp.PhraseBuilder
import com.docer.engine.nlp.UsageExampleBuilder
import com.docer.engine.style.ResolvedStyle
import com.docer.engine.template.TemplateEngine
import com.dartdocer.style.DartStyleLoader
import com.jetbrains.lang.dart.psi.DartFormalParameterList
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBodyOrNative
import com.jetbrains.lang.dart.psi.DartMethodDeclaration

/**
 * Generates DartDoc for functions and methods. Parameters are mentioned as `[name]`
 * inline references, return values as "Returns the..." prose, and async/Stream notes
 * are added automatically.
 */
internal class DartFunctionDocGenerator(
    private val project: Project,
    private val element: PsiElement,
) : DartDocGenerator {

    override fun generate(): String {
        val style = DartStyleLoader.resolve(project)
        val t = style.template
        val asyncInfo = DartAsyncAnalyzer.analyze(element)
        val aspects = DartAspectEngine.analyze(element, style)

        val name = getName()
        val returnTypeText = getReturnTypeText()
        val paramNames = getParamNames()
        val hasReturn = returnTypeText != null && returnTypeText != "void"

        // Nothing to document: emit a minimal stub.
        val isEmpty = !style.appendName && paramNames.isEmpty() && !hasReturn &&
            !asyncInfo.hasNotes && aspects.isEmpty
        if (isEmpty) return "///\n"

        val builder = StringBuilder()

        // Description line.
        if (style.appendName) {
            val description = describe(style, returnTypeText)
            builder.appendDocLine(TemplateEngine.render(t.functionDescription, mapOf("description" to description)))
        } else {
            builder.appendDocLine()
        }

        // Async / suspension notes.
        if (asyncInfo.noteLines.isNotEmpty()) {
            builder.appendEmptyDocLine()
            asyncInfo.noteLines.forEach { builder.appendDocLine(it) }
        }

        // Framework-aware notes.
        if (aspects.notes.isNotEmpty()) {
            builder.appendEmptyDocLine()
            aspects.notes.forEach { builder.appendDocLine(it) }
        }

        // Parameters mentioned as [paramName] inline references.
        if (paramNames.isNotEmpty()) {
            builder.appendEmptyDocLine()
            val paramRefs = paramNames.joinToString(", ") { "[$it]" }
            builder.appendDocLine("Takes $paramRefs.")
        }

        // Return description as prose.
        if (hasReturn) {
            builder.appendEmptyDocLine()
            val returnDescription = asyncInfo.returnDescription
                ?: PhraseBuilder.returnPhrase(returnTypeText, PhraseBuilder.DART_COLLECTION_TYPES, setOf("void"))
                ?: ""
            if (returnDescription.isNotBlank()) {
                builder.appendDocLine(TemplateEngine.render(t.returnLine, mapOf("description" to returnDescription)))
            }
        }

        // Throws detection.
        if (style.throwsDetection) {
            val thrown = DartThrowsDetector.detect(element)
            if (thrown.isNotEmpty()) {
                builder.appendEmptyDocLine()
                thrown.forEach { exceptionType ->
                    builder.appendDocLine("Throws [$exceptionType].")
                }
            }
        }

        // Optional usage example.
        if (style.usageExample) {
            val example = UsageExampleBuilder.forFunction(
                name = name ?: "",
                paramNames = paramNames,
                returnTypeText = returnTypeText,
                receiverTypeText = null,
                language = UsageExampleBuilder.Language.DART,
            )
            if (example.isNotEmpty()) {
                builder.appendEmptyDocLine()
                example.forEach { builder.appendDocLine(it) }
            }
        }

        // See references (override -> super method).
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

    private fun describe(style: ResolvedStyle, returnType: String?): String {
        val funcName = getName() ?: "Function"
        return if (!style.splitNames) funcName
        else PhraseBuilder.describe(
            name = funcName,
            returnTypeText = returnType,
            verbMapping = style.verbMapping,
            collectionTypes = PhraseBuilder.DART_COLLECTION_TYPES,
        )
    }

    private fun getName(): String? = when (element) {
        is DartFunctionDeclarationWithBodyOrNative -> element.componentName?.text
        is DartMethodDeclaration -> element.name
        else -> null
    }

    private fun getReturnTypeText(): String? = when (element) {
        is DartFunctionDeclarationWithBodyOrNative -> element.returnType?.text
        is DartMethodDeclaration -> element.returnType?.text
        else -> null
    }

    private fun getParamNames(): List<String> {
        val paramList: DartFormalParameterList? = when (element) {
            is DartFunctionDeclarationWithBodyOrNative -> element.formalParameterList
            is DartMethodDeclaration -> element.formalParameterList
            else -> null
        }
        if (paramList == null) return emptyList()

        val names = mutableListOf<String>()
        // Normal positional parameters - use findComponentName()
        paramList.normalFormalParameterList.forEach { param ->
            param.findComponentName()?.text?.let { names += it }
        }
        // Optional/named parameters
        paramList.optionalFormalParameters?.let { optional ->
            optional.defaultFormalNamedParameterList.forEach { param ->
                param.normalFormalParameter?.findComponentName()?.text?.let { names += it }
            }
        }
        return names
    }
}
