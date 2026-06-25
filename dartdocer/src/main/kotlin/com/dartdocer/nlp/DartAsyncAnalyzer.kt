package com.dartdocer.nlp

import com.intellij.psi.PsiElement
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBodyOrNative
import com.jetbrains.lang.dart.psi.DartMethodDeclaration

/**
 * Inspects a Dart function/method for async patterns so the generated DartDoc can
 * describe asynchronous and stream-based return types accurately.
 */
object DartAsyncAnalyzer {

    data class AsyncInfo(
        val isAsync: Boolean,
        val isAsyncStar: Boolean,
        val noteLines: List<String>,
        val returnDescription: String?,
    ) {
        val hasNotes: Boolean get() = isAsync || isAsyncStar || noteLines.isNotEmpty()
    }

    private val GENERIC = Regex("^([A-Za-z_][\\w.]*)\\s*<\\s*(.+)\\s*>$")

    fun analyze(element: PsiElement): AsyncInfo {
        val bodyText = getBodyText(element)
        val returnTypeText = getReturnTypeText(element)

        val isAsync = bodyText?.contains("async") == true && bodyText.contains("async*").not()
        val isAsyncStar = bodyText?.contains("async*") == true

        val notes = mutableListOf<String>()
        if (isAsync) {
            notes += "Asynchronous function — returns a [Future]."
        }
        if (isAsyncStar) {
            notes += "Generator function — yields values via a [Stream]."
        }

        val returnDescription = returnTypeText?.let { reactiveReturn(it) }
        return AsyncInfo(isAsync, isAsyncStar, notes, returnDescription)
    }

    private fun getBodyText(element: PsiElement): String? = when (element) {
        is DartFunctionDeclarationWithBodyOrNative -> element.functionBody?.text
        is DartMethodDeclaration -> element.functionBody?.text
        else -> null
    }

    private fun getReturnTypeText(element: PsiElement): String? = when (element) {
        is DartFunctionDeclarationWithBodyOrNative -> element.returnType?.text
        is DartMethodDeclaration -> element.returnType?.text
        else -> null
    }

    /** Produces a return description for known async wrapper types, or `null`. */
    private fun reactiveReturn(typeText: String): String? {
        val match = GENERIC.find(typeText.trim()) ?: return null
        val outer = match.groupValues[1].substringAfterLast('.')
        val arg = match.groupValues[2].trim()
        return when (outer) {
            "Future" -> "a [Future] that resolves to [$arg]"
            "FutureOr" -> "a [FutureOr] that resolves to [$arg]"
            "Stream" -> "a [Stream] emitting [$arg] values"
            else -> null
        }
    }
}
