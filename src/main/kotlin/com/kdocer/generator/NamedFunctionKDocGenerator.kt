package com.kdocer.generator

import com.intellij.openapi.project.Project
import com.kdocer.util.Validator
import org.jetbrains.kotlin.psi.KtFunctionType
import org.jetbrains.kotlin.psi.KtNamedFunction

class NamedFunctionKDocGenerator(private val project: Project, private val element: KtNamedFunction) :
    KDocGenerator {
    override fun generate(): String {
        val isAppendName = Validator.isAppendName()

        // Return an empty KDoc, if applicable
        val isEmpty = !isAppendName && element.typeParameters.isEmpty() && element.valueParameters.isEmpty()
                && (element.typeReference?.text ?: "Unit") == "Unit"
        if (isEmpty)
            return "/**\n * \n */\n"

        val builder = StringBuilder()
        val nameToPhrase = if (Validator.isNameNeedsSplit()) nameToPhrase(element.name ?: "Function") else element.name
        builder.appendLine("/**")
            .append("* ").apply { if (isAppendName) append(nameToPhrase) }.appendLine()
            .appendLine("*")

        if (element.typeParameters.isNotEmpty()) {
            builder.appendLine(toParamsKdoc(params = element.typeParameters))
        }
        if (element.valueParameters.isNotEmpty()) {
            builder.appendLine(toParamsKdoc(params = element.valueParameters))
            element.valueParameters.forEach end@{
                if (it.typeReference != null && it.typeReference?.typeElement is KtFunctionType) {
                    builder.appendLine("* @receiver")
                    return@end
                }
            }
        }
        element.typeReference?.let {
            if (it.text != "Unit") {
                builder.appendLine("* @return")
            }
        }


        builder.appendLine("*/")
        return builder.toString()
    }
}