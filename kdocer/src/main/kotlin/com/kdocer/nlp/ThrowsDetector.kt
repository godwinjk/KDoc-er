package com.kdocer.nlp

import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtThrowExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

object ThrowsDetector {

    fun detect(function: KtNamedFunction): List<String> {
        val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()
        return body.collectDescendantsOfType<KtThrowExpression>()
            .mapNotNull { throwExpr ->
                val calleeText = throwExpr.thrownExpression?.text ?: return@mapNotNull null
                val typeName = calleeText.substringBefore('(').substringAfterLast('.').trim()
                typeName.ifBlank { null }
            }
            .distinct()
            .sorted()
    }
}
