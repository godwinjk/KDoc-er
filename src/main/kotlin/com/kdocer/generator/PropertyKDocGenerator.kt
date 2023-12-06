package com.kdocer.generator

import com.intellij.openapi.project.Project
import com.kdocer.util.Validator
import org.jetbrains.kotlin.psi.KtProperty

class PropertyKDocGenerator(private val project: Project, private val element: KtProperty) :
    KDocGenerator {
    override fun generate(): String {
        val isAppendName = Validator.isAppendName()

        // Return an empty KDoc, if applicable
        val isEmpty = !isAppendName
        if (isEmpty)
            return "/**\n *\n */\n"

        val builder = StringBuilder()
        val nameToPhrase = if (Validator.isNameNeedsSplit()) nameToPhrase(element.name ?: "Property") else element.name
        builder.appendLine("/**")
            .append("* ").apply { if (isAppendName) append(nameToPhrase) }.appendLine()
//            .appendLine("*")

//        if (element.typeParameters.isNotEmpty()) {
//            builder.appendLine(toParamsKdoc(params = element.typeParameters))
//        }
//        if (element.valueParameters.isNotEmpty()) {
//            builder.appendLine(toParamsKdoc(params = element.valueParameters))
//        }
//        element.typeReference?.let {
//            if (it.text != "Unit") {
//                builder.appendLine("* @return")
//            }
//        }
        builder.appendLine("*/")
        return builder.toString()
    }
}