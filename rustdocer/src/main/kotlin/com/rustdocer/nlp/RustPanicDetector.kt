package com.rustdocer.nlp

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsMacroCall
import org.rust.lang.core.psi.RsMethodCallExpr
import org.rust.lang.core.psi.ext.block
import org.rust.lang.core.psi.ext.macroName

/**
 * Scans a Rust function body for panic conditions: `panic!()`, `.unwrap()`,
 * `.expect()`, `todo!()`, and `unimplemented!()` macro invocations.
 */
object RustPanicDetector {

    data class PanicCondition(val description: String)

    private val PANIC_MACROS = setOf("panic", "todo", "unimplemented", "unreachable")
    private val PANIC_METHODS = setOf("unwrap", "expect")

    fun detect(function: RsFunction): List<PanicCondition> {
        val body = function.block ?: return emptyList()
        val conditions = mutableListOf<PanicCondition>()
        val seen = mutableSetOf<String>()

        // Detect panic-inducing macro calls
        PsiTreeUtil.collectElementsOfType(body, RsMacroCall::class.java).forEach { macroCall ->
            val name = macroCall.macroName
            if (name in PANIC_MACROS && seen.add(name)) {
                conditions.add(panicConditionFor(name, macroCall))
            }
        }

        // Detect .unwrap() and .expect() method calls
        PsiTreeUtil.collectElementsOfType(body, RsMethodCallExpr::class.java).forEach { methodCallExpr ->
            val methodName = methodCallExpr.methodCall?.identifier?.text
            if (methodName != null && methodName in PANIC_METHODS && seen.add(methodName)) {
                conditions.add(panicConditionFor(methodName, methodCallExpr))
            }
        }

        return conditions
    }

    private fun panicConditionFor(name: String, element: PsiElement): PanicCondition = when (name) {
        "panic" -> PanicCondition("Panics if the internal invariant is violated.")
        "unwrap" -> PanicCondition("Panics if an `Option` is `None` or a `Result` is `Err`.")
        "expect" -> PanicCondition("Panics if an expected condition is not met.")
        "todo" -> PanicCondition("Panics because the implementation is incomplete (`todo!()`).")
        "unimplemented" -> PanicCondition("Panics because the function is not yet implemented (`unimplemented!()`).")
        "unreachable" -> PanicCondition("Panics if an unreachable code path is reached.")
        else -> PanicCondition("May panic under certain conditions.")
    }
}
