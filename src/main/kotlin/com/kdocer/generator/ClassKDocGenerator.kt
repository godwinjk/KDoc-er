package com.kdocer.generator

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.util.isExpectDeclaration
import org.jetbrains.kotlin.psi.KtClass

/**
 * Class k doc generator
 *
 * @property project
 * @property element
 * @constructor Create empty Class k doc generator

 */
class ClassKDocGenerator(private val project: Project, private val element: KtClass) :
    KDocGenerator {

    override fun generate(): String {
        val builder = StringBuilder()
        val name = nameToPhrase(element.name ?: "Class")
        builder.appendLine("/**")
            .appendLine("* $name")
            .appendLine("*")

        if (element.typeParameters.isNotEmpty()) {
            builder.appendLine(toParamsKdoc(params = element.typeParameters))
        }

        val (properties, parameters) = element.primaryConstructor?.valueParameters?.partition {
            it.hasValOrVar()
        } ?: Pair(emptyList(), emptyList())

        if (properties.isNotEmpty()) {
            builder.appendLine(toParamsKdoc(keyword = "@property", params = properties))
        }

        if (parameters.isNotEmpty()) {
            builder.appendLine("* @constructor")
                .appendLine("*")
                .appendLine(toParamsKdoc(params = parameters))
        } else {
            builder.appendLine("* @constructor Create empty $name")
                .appendLine(toParamsKdoc(params = parameters))
        }
        if (element.isExpectDeclaration()) {
            builder.appendLine("* @exception")
        }
        builder.appendLine("*/")
        return builder.toString()
    }
}