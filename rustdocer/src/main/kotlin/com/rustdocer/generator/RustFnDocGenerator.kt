package com.rustdocer.generator

import com.intellij.openapi.project.Project
import com.docer.engine.nlp.PhraseBuilder
import com.docer.engine.nlp.UsageExampleBuilder
import com.docer.engine.nlp.WordSplitter
import com.docer.engine.style.ResolvedStyle
import com.docer.engine.template.TemplateEngine
import com.rustdocer.aspect.RustAspectEngine
import com.rustdocer.nlp.RustAsyncAnalyzer
import com.rustdocer.nlp.RustErrorAnalyzer
import com.rustdocer.nlp.RustPanicDetector
import com.rustdocer.nlp.RustUnsafeAnalyzer
import com.rustdocer.service.RustDocerSettings
import com.rustdocer.style.RustStyleLoader
import org.rust.lang.core.psi.RsFunction

class RustFnDocGenerator(private val project: Project, private val element: RsFunction) :
    RustDocGenerator {

    override fun generate(): String {
        val style = RustStyleLoader.resolve(project)
        val settings = RustDocerSettings.getInstance()
        val t = style.template
        val asyncInfo = RustAsyncAnalyzer.analyze(element)
        val aspects = RustAspectEngine.analyze(element, style)

        val returnTypeText = element.retType?.typeReference?.text
        val hasReturn = returnTypeText != null && returnTypeText.trim() != "()"
        val params = element.valueParameterList?.valueParameterList ?: emptyList()

        // Nothing to document: emit a minimal stub.
        val isEmpty = !style.appendName && params.isEmpty() && !hasReturn &&
            !asyncInfo.hasNotes && aspects.isEmpty
        if (isEmpty) return "///\n"

        val builder = StringBuilder()

        // Description line.
        if (style.appendName) {
            val description = describe(style, returnTypeText)
            val rendered = TemplateEngine.render(t.functionDescription, mapOf("description" to description))
            builder.appendDocLine(rendered)
        } else {
            builder.appendDocLine()
        }

        // Async notes.
        asyncInfo.noteLines.forEach { builder.appendDocLine(it) }

        // Framework-aware / crate-aware notes.
        aspects.notes.forEach { builder.appendDocLine(it) }

        // Usage example.
        if (style.usageExample) {
            val example = UsageExampleBuilder.forFunction(
                name = element.name ?: "",
                paramNames = params.mapNotNull { it.pat?.text },
                returnTypeText = returnTypeText,
                receiverTypeText = null,
                language = UsageExampleBuilder.Language.RUST,
            )
            if (example.isNotEmpty()) {
                builder.appendEmptyDocLine()
                builder.appendDocLine("# Examples")
                builder.appendEmptyDocLine()
                example.forEach { builder.appendDocLine(it) }
            }
        }

        // # Arguments section.
        if (params.isNotEmpty()) {
            val selfParams = params.filter { isSelfParam(it.pat?.text) }
            val nonSelfParams = params.filter { !isSelfParam(it.pat?.text) }
            if (nonSelfParams.isNotEmpty()) {
                builder.appendEmptyDocLine()
                builder.appendDocLine("# Arguments")
                builder.appendEmptyDocLine()
                nonSelfParams.forEach { param ->
                    val paramName = param.pat?.text ?: ""
                    val noun = WordSplitter.nounOrEmpty(paramName)
                    val rendered = TemplateEngine.render(
                        t.paramLine,
                        mapOf("name" to paramName, "noun" to noun),
                    )
                    builder.appendDocLine(rendered)
                }
            }
        }

        // # Returns section.
        if (hasReturn) {
            val returnDescription = asyncInfo.returnDescription
                ?: PhraseBuilder.returnPhrase(returnTypeText, PhraseBuilder.RUST_COLLECTION_TYPES, setOf("()"))
                ?: ""
            if (returnDescription.isNotBlank()) {
                builder.appendEmptyDocLine()
                builder.appendDocLine("# Returns")
                builder.appendEmptyDocLine()
                val rendered = TemplateEngine.render(t.returnLine, mapOf("description" to returnDescription))
                builder.appendDocLine(rendered)
            }
        }

        // # Panics section.
        if (settings.isPanicDetection) {
            val panics = RustPanicDetector.detect(element)
            if (panics.isNotEmpty()) {
                builder.appendEmptyDocLine()
                builder.appendDocLine("# Panics")
                builder.appendEmptyDocLine()
                panics.forEach { builder.appendDocLine(it.description) }
            }
        }

        // # Safety section for unsafe functions.
        if (settings.isUnsafeSafetySection) {
            val unsafeInfo = RustUnsafeAnalyzer.analyze(element)
            if (unsafeInfo.shouldEmitSafetySection) {
                builder.appendEmptyDocLine()
                builder.appendDocLine("# Safety")
                builder.appendEmptyDocLine()
                builder.appendDocLine("Callers must ensure the safety invariants are upheld.")
            }
        }

        // # Errors section for Result returns.
        if (settings.isErrorsSection) {
            val errorInfo = RustErrorAnalyzer.analyze(element)
            if (errorInfo.returnsResult) {
                builder.appendEmptyDocLine()
                builder.appendDocLine("# Errors")
                builder.appendEmptyDocLine()
                val errorDesc = if (errorInfo.errorType != null) {
                    "Returns `Err(${errorInfo.errorType})` if the operation fails."
                } else {
                    "Returns an error if the operation fails."
                }
                builder.appendDocLine(errorDesc)
            }
        }

        // See references.
        if (style.seeReferences) {
            val refs = com.rustdocer.nlp.RustSeeReferenceResolver.resolve(element)
            if (refs.isNotEmpty()) {
                builder.appendEmptyDocLine()
                refs.forEach { builder.appendDocLine("See also: [`$it`]") }
            }
        }

        // Framework-aware tag lines.
        aspects.tags.forEach { builder.appendDocLine(it) }

        // Since tag as a trailing note.
        if (style.sinceTag && style.sinceVersion.isNotBlank()) {
            builder.appendEmptyDocLine()
            builder.appendDocLine("Since: ${style.sinceVersion}")
        }

        return builder.toString()
    }

    private fun describe(style: ResolvedStyle, returnType: String?): String {
        val name = element.name ?: "function"
        return if (!style.splitNames) name
        else PhraseBuilder.describe(
            name = name,
            returnTypeText = returnType,
            receiverTypeText = null,
            verbMapping = style.verbMapping,
            collectionTypes = PhraseBuilder.RUST_COLLECTION_TYPES,
        )
    }

    private fun isSelfParam(text: String?): Boolean {
        if (text == null) return false
        val trimmed = text.trim()
        return trimmed == "self" || trimmed == "&self" || trimmed == "&mut self" || trimmed == "mut self"
    }
}
