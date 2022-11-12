package com.kdocer.generator

import com.intellij.openapi.project.Project
import com.kdocer.service.KDocerSettings
import com.kdocer.util.Validator
import org.jetbrains.kotlin.psi.KtClassOrObject

/**
 * Class k doc generator
 *
 * @property project
 * @property element
 * @constructor Create empty Class k doc generator
 */
internal class ClassKDocGenerator(private val project: Project, private val element: KtClassOrObject) :
    KDocGenerator {

    override fun generate(): String {
        val settings = KDocerSettings.getInstance()
        val builder = StringBuilder()
        val name = if (Validator.isNameNeedsSplit()) nameToPhrase(element.name ?: "Class") else element.name
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
            if (settings.isAllowedEmptyConstructor) {
                builder.appendLine("* @constructor Create empty $name")
            }
        }
        builder.appendLine("*/")
        return builder.toString()
    }
}