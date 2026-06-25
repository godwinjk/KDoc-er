package com.dartdocer.nlp

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBodyOrNative
import com.jetbrains.lang.dart.psi.DartMethodDeclaration
import com.jetbrains.lang.dart.psi.DartThrowExpression

/**
 * Scans a Dart function/method body for `throw` expressions and extracts the exception
 * type names for documentation.
 */
object DartThrowsDetector {

    fun detect(element: PsiElement): List<String> {
        val body = when (element) {
            is DartFunctionDeclarationWithBodyOrNative -> element.functionBody
            is DartMethodDeclaration -> element.functionBody
            else -> null
        } ?: return emptyList()

        return PsiTreeUtil.collectElementsOfType(body, DartThrowExpression::class.java)
            .mapNotNull { throwExpr ->
                val thrownText = throwExpr.expression?.text ?: return@mapNotNull null
                // Extract the type name before the constructor call parentheses
                val typeName = thrownText.substringBefore('(').substringAfterLast('.').trim()
                typeName.ifBlank { null }
            }
            .distinct()
            .sorted()
    }
}
