package com.kdocer.nlp

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Inspects a function for coroutine and asynchronous-stream characteristics so the
 * generated KDoc can describe suspension and reactive return types accurately.
 */
object CoroutineAnalyzer {

    /**
     * @property isSuspend whether the function carries the `suspend` modifier
     * @property noteLines extra description lines (without the leading `* `) to add to the body
     * @property returnDescription a tailored `@return` description for reactive types, or `null`
     */
    data class CoroutineInfo(
        val isSuspend: Boolean,
        val noteLines: List<String>,
        val returnDescription: String?,
    ) {
        val hasNotes: Boolean get() = isSuspend || noteLines.isNotEmpty()
    }

    private val GENERIC = Regex("^([A-Za-z_][\\w.]*)\\s*<\\s*(.+)\\s*>$")

    fun analyze(function: KtNamedFunction): CoroutineInfo {
        val isSuspend = function.hasModifier(KtTokens.SUSPEND_KEYWORD)
        val notes = mutableListOf<String>()
        if (isSuspend) {
            notes += "Suspending function — must be called from a coroutine or another suspending function."
        }
        val returnDescription = function.typeReference?.text?.let { reactiveReturn(it) }
        return CoroutineInfo(isSuspend, notes, returnDescription)
    }

    /** Produces a `@return` description for known reactive wrapper types, or `null`. */
    private fun reactiveReturn(typeText: String): String? {
        val match = GENERIC.find(typeText.trim()) ?: return null
        val outer = match.groupValues[1].substringAfterLast('.')
        val arg = match.groupValues[2].trim()
        return when (outer) {
            "Flow" -> "a cold [Flow] emitting [$arg] values"
            "StateFlow" -> "a [StateFlow] holding the current [$arg] value"
            "SharedFlow" -> "a hot [SharedFlow] emitting [$arg] values"
            "Deferred" -> "a [Deferred] that resolves to [$arg]"
            "Channel", "ReceiveChannel", "SendChannel" -> "a [$outer] of [$arg]"
            "LiveData", "MutableLiveData" -> "a [$outer] of [$arg]"
            else -> null
        }
    }
}
