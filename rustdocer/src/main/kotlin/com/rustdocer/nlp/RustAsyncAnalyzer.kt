package com.rustdocer.nlp

import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.ext.isAsync

/**
 * Detects async patterns in Rust functions:
 * - `async fn` keyword
 * - Return type `impl Future<Output=T>`
 * - Return type `impl Stream<Item=T>`
 */
object RustAsyncAnalyzer {

    data class AsyncInfo(
        val isAsync: Boolean,
        val noteLines: List<String>,
        val returnDescription: String?,
    ) {
        val hasNotes: Boolean get() = isAsync || noteLines.isNotEmpty()
    }

    private val IMPL_FUTURE = Regex("impl\\s+Future\\s*<\\s*Output\\s*=\\s*(.+?)\\s*>")
    private val IMPL_STREAM = Regex("impl\\s+Stream\\s*<\\s*Item\\s*=\\s*(.+?)\\s*>")

    fun analyze(function: RsFunction): AsyncInfo {
        val isAsync = function.isAsync
        val notes = mutableListOf<String>()
        var returnDescription: String? = null

        if (isAsync) {
            notes += "Async function — returns a `Future` that must be `.await`ed."
        }

        val retTypeText = function.retType?.typeReference?.text
        if (retTypeText != null) {
            val futureMatch = IMPL_FUTURE.find(retTypeText)
            val streamMatch = IMPL_STREAM.find(retTypeText)

            when {
                futureMatch != null -> {
                    val outputType = futureMatch.groupValues[1].trim()
                    if (!isAsync) {
                        notes += "Returns a `Future` that resolves to `$outputType`."
                    }
                    returnDescription = "a `Future` that resolves to `$outputType`"
                }
                streamMatch != null -> {
                    val itemType = streamMatch.groupValues[1].trim()
                    notes += "Returns a `Stream` that yields `$itemType` values."
                    returnDescription = "a `Stream` yielding `$itemType` values"
                }
            }
        }

        return AsyncInfo(isAsync, notes, returnDescription)
    }
}
