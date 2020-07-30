package com.kdocer.generator

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtProperty

class PropertyKDocGenerator(private val project: Project, private val property: KtProperty) :
    KDocGenerator {
    override fun generate(): String {

        val builder = StringBuilder()
        val nameToPhrase = nameToPhrase(property.name ?: "Function")
        builder.appendLine("/**")
            .appendLine("* $nameToPhrase")
            .appendLine("*")

        if (property.typeParameters.isNotEmpty()) {
            builder.appendLine(toParamsKdoc(params = property.typeParameters))
        }
        if (property.valueParameters.isNotEmpty()) {
            builder.appendLine(toParamsKdoc(params = property.valueParameters))
        }
        property.typeReference?.let {
            if (it.text != "Unit") {
                builder.appendLine("* @return")
            }
        }
        builder.appendLine("*/")
        return builder.toString()
    }
}