package com.kdocer.generator

import com.intellij.openapi.project.Project
import com.kdocer.aspect.AspectEngine
import com.kdocer.nlp.CoroutineAnalyzer
import com.kdocer.nlp.PhraseBuilder
import com.kdocer.nlp.SeeReferenceResolver
import com.kdocer.nlp.ThrowsDetector
import com.kdocer.nlp.UsageExampleBuilder
import com.kdocer.nlp.WordSplitter
import com.kdocer.style.ResolvedStyle
import com.kdocer.style.StyleLoader
import com.kdocer.template.TemplateEngine
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.permissions.KaAllowAnalysisOnEdt
import org.jetbrains.kotlin.analysis.api.permissions.allowAnalysisOnEdt
import org.jetbrains.kotlin.analysis.api.renderer.types.impl.KaTypeRendererForSource
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.psi.KtFunctionType
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.types.Variance

class NamedFunctionKDocGenerator(private val project: Project, private val element: KtNamedFunction) :
    KDocGenerator {

    override fun generate(): String {
        val style = StyleLoader.resolve(project)
        val t = style.template
        val coroutine = CoroutineAnalyzer.analyze(element)
        val aspects = AspectEngine.analyze(element, style)

        // Explicit type when present, otherwise resolve an expression body's inferred type.
        val returnType = element.typeReference?.text ?: inferredReturnType()
        val hasReturn = (returnType ?: "Unit") != "Unit"

        // Nothing to document and nothing to say: emit a minimal stub.
        val isEmpty = !style.appendName && element.typeParameters.isEmpty() &&
                element.valueParameters.isEmpty() && !hasReturn && !coroutine.hasNotes && aspects.isEmpty
        if (isEmpty) return "/**\n * \n */\n"

        val builder = StringBuilder()
        builder.appendLine("/**")

        // Description line.
        builder.append("* ")
        if (style.appendName) {
            val description = describe(style, returnType)
            builder.append(TemplateEngine.render(t.functionDescription, mapOf("description" to description)))
        }
        builder.appendLine()

        // Coroutine / suspension and framework-aware notes.
        coroutine.noteLines.forEach { builder.appendLine("* $it") }
        aspects.notes.forEach { builder.appendLine("* $it") }

        // Optional usage example.
        if (style.usageExample) {
            val example = UsageExampleBuilder.forFunction(
                name = element.name ?: "",
                paramNames = element.valueParameters.mapNotNull { it.name },
                returnTypeText = returnType,
                receiverTypeText = element.receiverTypeReference?.text,
            )
            if (example.isNotEmpty()) {
                builder.appendLine("*")
                example.forEach { builder.appendLine("* $it") }
            }
        }
        builder.appendLine("*")

        // Type parameters.
        element.typeParameters.forEach {
            builder.appendLine(TemplateEngine.render(t.typeParamLine, mapOf("name" to (it.name ?: ""))))
        }

        // Value parameters.
        element.valueParameters.forEach { param ->
            builder.appendLine(
                TemplateEngine.render(
                    t.paramLine,
                    mapOf("name" to (param.name ?: ""), "noun" to WordSplitter.nounOrEmpty(param.name ?: "")),
                )
            )
            // Preserve original behaviour: a function-typed parameter documents a receiver.
            if (param.typeReference?.typeElement is KtFunctionType) {
                builder.appendLine("* @receiver")
            }
        }

        // Return value — coroutine/Flow note wins, otherwise a short type-derived phrase
        // so @return is meaningful rather than blank.
        if (hasReturn) {
            val returnDescription = coroutine.returnDescription ?: PhraseBuilder.returnPhrase(returnType) ?: ""
            if (returnDescription.isNotBlank()) {
                builder.appendLine(TemplateEngine.render(t.returnLine, mapOf("description" to returnDescription)))
            }
        }

        // @throws tags from throw-expression detection.
        if (style.throwsDetection) {
            ThrowsDetector.detect(element).forEach { exceptionType ->
                builder.appendLine("* @throws $exceptionType")
            }
        }

        // @see cross-references (override → super method).
        if (style.seeReferences) {
            SeeReferenceResolver.resolve(element).forEach { ref ->
                builder.appendLine("* @see $ref")
            }
        }

        // Framework-aware tag lines (e.g. @see).
        aspects.tags.forEach { builder.appendLine("* $it") }

        // @since version stamp.
        if (style.sinceTag && style.sinceVersion.isNotBlank()) {
            builder.appendLine("* @since ${style.sinceVersion}")
        }

        builder.appendLine("*/")
        return builder.toString()
    }

    private fun describe(style: ResolvedStyle, returnType: String?): String {
        val name = element.name ?: "Function"
        return if (!style.splitNames) name
        else PhraseBuilder.describe(
            name = name,
            returnTypeText = returnType,
            receiverTypeText = element.receiverTypeReference?.text,
            verbMapping = style.verbMapping,
        )
    }

    /**
     * Resolves the inferred return type of an expression-body function (`fun f() = expr`)
     * via the K2 Analysis API. Returns `null` for block bodies, `Unit`, or on any failure.
     */
    @OptIn(KaExperimentalApi::class, KaAllowAnalysisOnEdt::class)
    private fun inferredReturnType(): String? {
        if (element.bodyExpression == null || element.hasBlockBody()) return null
        return try {
            allowAnalysisOnEdt {
                analyze(element) {
                    val symbol = element.symbol as? KaNamedFunctionSymbol ?: return@analyze null
                    symbol.returnType.render(KaTypeRendererForSource.WITH_SHORT_NAMES, position = Variance.INVARIANT)
                }
            }?.takeUnless { it == "Unit" }
        } catch (t: Throwable) {
            null
        }
    }
}
