package com.dartdocer.style

import com.intellij.openapi.project.Project
import com.docer.engine.style.ResolvedStyle
import com.docer.engine.style.StyleSheetReader
import com.docer.engine.template.DocTemplate
import com.dartdocer.service.DartDocerSettings

object DartStyleLoader {

    fun resolve(project: Project): ResolvedStyle {
        val sheet = project.basePath?.let { StyleSheetReader.read(it, ".dartdocer.yaml") }
        val settings = DartDocerSettings.getInstance()
        val default = DocTemplate.DARTDOC_DEFAULT

        val template = DocTemplate(
            functionDescription = pick(sheet?.functionDescription, settings.templateFunctionDescription, default.functionDescription),
            paramLine = default.paramLine,
            returnLine = pick(sheet?.returnLine, settings.templateReturn, default.returnLine),
            classDescription = pick(sheet?.classDescription, settings.templateClassDescription, default.classDescription),
            propertyDescription = pick(sheet?.propertyDescription, settings.templateFieldDescription, default.propertyDescription),
            constructorLine = pick(sheet?.constructorLine, settings.templateConstructor, default.constructorLine),
            constructorEmptyLine = default.constructorEmptyLine,
        )

        return ResolvedStyle(
            appendName = sheet?.appendName ?: settings.isAppendName,
            splitNames = sheet?.splitNames ?: settings.isSplitNames,
            verbMapping = sheet?.verbMapping ?: emptyMap(),
            frameworkAware = sheet?.frameworkAware ?: settings.isFlutterAware,
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
