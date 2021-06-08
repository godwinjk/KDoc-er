package com.kdocer.generator

import com.intellij.openapi.project.Project
import com.kdocer.util.Validator
import org.jetbrains.kotlin.psi.KtProperty

class PropertyKDocGenerator(private val project: Project, private val element: KtProperty) :
    KDocGenerator {
    override fun generate(): String {

        val builder = StringBuilder()
        val nameToPhrase = if (Validator.isNameNeedsSplit()) nameToPhrase(element.name ?: "Property") else element.name
        builder.appendLine("/**")
            .appendLine("* $nameToPhrase")
//            .appendLine("*")

//        if (element.typeParameters.isNotEmpty()) {
//            builder.appendLine(toParamsKdoc(params = element.typeParameters))
//        }
//        if (element.valueParameters.isNotEmpty()) {
//            builder.appendLine(toParamsKdoc(params = element.valueParameters))
//        }
        element.typeReference?.let {
            if (it.text != "Unit") {
                builder.appendLine(toReturnKdoc(it.text))
            }
        }

        builder.appendLine("*/")
        return builder.toString()
    }
}
