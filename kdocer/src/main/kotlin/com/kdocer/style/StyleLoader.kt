package com.kdocer.style

import com.intellij.openapi.project.Project
import com.docer.engine.style.ResolvedStyle
import com.docer.engine.style.StyleSheetReader
import com.docer.engine.template.DocTemplate
import com.kdocer.service.KDocerSettings

object StyleLoader {

    fun resolve(project: Project): ResolvedStyle {
        val sheet = project.basePath?.let { StyleSheetReader.read(it, ".kdocer.yaml") }
        val settings = KDocerSettings.getInstance()
        val default = DocTemplate.KDOC_DEFAULT

        val template = DocTemplate(
            functionDescription = pick(sheet?.functionDescription, settings.templateFunctionDescription, default.functionDescription),
            paramLine = pick(sheet?.paramLine, settings.templateParam, default.paramLine),
            returnLine = pick(sheet?.returnLine, settings.templateReturn, default.returnLine),
            classDescription = pick(sheet?.classDescription, settings.templateClassDescription, default.classDescription),
            propertyDescription = pick(sheet?.propertyDescription, settings.templatePropertyDescription, default.propertyDescription),
            constructorLine = pick(sheet?.constructorLine, settings.templateConstructor, default.constructorLine),
        )

        return ResolvedStyle(
            appendName = sheet?.appendName ?: settings.isAppendName,
            splitNames = sheet?.splitNames ?: settings.isSplittedClassNames,
            verbMapping = sheet?.verbMapping ?: emptyMap(),
            frameworkAware = sheet?.frameworkAware ?: settings.isFrameworkAware,
            aspectNotes = sheet?.aspectNotes ?: emptyMap(),
            template = template,
            usageExample = sheet?.usageExample ?: settings.isUsageExample,
            includeConstructor = sheet?.includeConstructor ?: settings.isConstructorLine,
            existingDocPolicy = sheet?.existingDocPolicy ?: settings.existingDocPolicy,
            throwsDetection = sheet?.throwsDetection ?: settings.isThrowsDetection,
            sinceTag = sheet?.sinceTag ?: settings.isSinceTag,
            sinceVersion = sheet?.sinceVersion ?: settings.sinceVersion,
            seeReferences = sheet?.seeReferences ?: settings.isSeeReferences,
        )
    }

    private fun pick(fromSheet: String?, fromSettings: String, default: String): String =
        fromSheet ?: fromSettings.ifBlank { default }
}
