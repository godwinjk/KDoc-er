package com.rustdocer.nlp

import org.rust.lang.core.psi.RsFunction

/**
 * Detects whether a function returns `Result<T, E>` and extracts the error type
 * for the `# Errors` documentation section.
 */
object RustErrorAnalyzer {

    data class ErrorInfo(
        val returnsResult: Boolean,
        val errorType: String?,
        val successType: String?,
    )

    private val RESULT_REGEX = Regex("^(?:anyhow::)?Result\\s*<\\s*(.+?)\\s*(?:,\\s*(.+?)\\s*)?>$")

    fun analyze(function: RsFunction): ErrorInfo {
        val retTypeText = function.retType?.typeReference?.text ?: return ErrorInfo(false, null, null)
        return analyzeTypeText(retTypeText.trim())
    }

    fun analyzeTypeText(typeText: String): ErrorInfo {
        val match = RESULT_REGEX.find(typeText) ?: return ErrorInfo(false, null, null)
        val successType = match.groupValues[1].trim().ifEmpty { null }
        val errorType = match.groupValues.getOrNull(2)?.trim()?.ifEmpty { null }
        return ErrorInfo(
            returnsResult = true,
            errorType = errorType,
            successType = successType,
        )
    }
}
