package com.rustdocer.style

import com.intellij.openapi.project.Project
import com.docer.engine.style.ResolvedStyle
import com.docer.engine.style.StyleSheetReader
import com.docer.engine.template.DocTemplate
import com.rustdocer.service.RustDocerSettings

object RustStyleLoader {

    fun resolve(project: Project): ResolvedStyle {
        val sheet = project.basePath?.let { StyleSheetReader.read(it, ".rustdocer.yaml") }
        val settings = RustDocerSettings.getInstance()
        val default = DocTemplate.RUSTDOC_DEFAULT

        val template = DocTemplate(
            functionDescription = pick(sheet?.functionDescription, settings.templateFunctionDescription, default.functionDescription),
            paramLine = pick(sheet?.paramLine, settings.templateParam, default.paramLine),
            returnLine = pick(sheet?.returnLine, settings.templateReturn, default.returnLine),
            classDescription = pick(sheet?.classDescription, settings.templateStructDescription, default.classDescription),
            propertyDescription = pick(sheet?.propertyDescription, settings.templateFieldDescription, default.propertyDescription),
            propertyTagLine = pick(null, settings.templateFieldDescription, default.propertyTagLine),
        )

        return ResolvedStyle(
            appendName = sheet?.appendName ?: settings.isAppendName,
            splitNames = sheet?.splitNames ?: settings.isSplitNames,
            verbMapping = sheet?.verbMapping ?: emptyMap(),
            frameworkAware = sheet?.frameworkAware ?: settings.isFrameworkAware,
            aspectNotes = sheet?.aspectNotes ?: emptyMap(),
            template = template,
            usageExample = sheet?.usageExample ?: settings.isUsageExample,
            includeConstructor = false,
            existingDocPolicy = sheet?.existingDocPolicy ?: settings.existingDocPolicy,
            throwsDetection = sheet?.throwsDetection ?: settings.isPanicDetection,
            sinceTag = sheet?.sinceTag ?: settings.isSinceTag,
            sinceVersion = sheet?.sinceVersion ?: settings.sinceVersion,
            seeReferences = sheet?.seeReferences ?: settings.isSeeReferences,
        )
    }

    private fun pick(fromSheet: String?, fromSettings: String, default: String): String =
        fromSheet ?: fromSettings.ifBlank { default }
}
