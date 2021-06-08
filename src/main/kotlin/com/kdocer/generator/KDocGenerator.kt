package com.kdocer.generator

import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.elementType
import java.util.regex.Pattern

interface KDocGenerator {

    companion object {
        const val LF = "\n"
    }

    fun generate(): String

    fun toParamsKdoc(keyword: String = "@param", params: List<PsiNameIdentifierOwner>) : String =
        params
            .map {"$keyword [${it.name}][${Regex("\\w*").find(it.text.split(":").last().trim())?.value}]${if (it.text.split(":").last().trim().last() == '?') "?" else ""}${if(Regex("=.*").find(it.text.split(":").last().trim())?.value != null) " ${Regex("=.*").find(it.text.split(":").last().trim())?.value}" else ""}"}
            .joinToString(LF, transform = { "* $it" })

    fun toReturnKdoc(returnText: String) : String = "* @return [${Regex("\\w*").find(returnText.trim())?.value}]${if (returnText.trim().last() == '?') "?" else ""}"

    fun StringBuilder.appendLine(text: String): StringBuilder = append(text).append(LF)


    fun nameToPhrase(name:String):String{
        val array = name.split(Pattern.compile("(?<=[a-zA-Z])(?=[A-Z])"))
        val builder = StringBuilder()
        array.forEach {
            builder.append(it.toLowerCase())
            builder.append(" ")
        }
        val phrase = builder.toString()
        return phrase.capitalize()
    }
}

