package com.kdocer.style

import com.intellij.openapi.project.Project
import com.kdocer.service.KDocerSettings
import com.kdocer.template.KDocTemplate

/**
 * Central configuration resolver. Merges three layers, highest priority first:
 *
 *  1. `.kdocer.yaml` in the project root (optional, version-controllable)
 *  2. the IDE settings panel ([KDocerSettings])
 *  3. built-in [KDocTemplate.DEFAULT] values
 *
 * Both the generators and the settings UI read through this single resolver, so what the
 * user sees configured and what the engine produces never diverge.
 */
object StyleLoader {

    fun resolve(project: Project): ResolvedStyle {
        val sheet = StyleSheetReader.read(project)
        val settings = KDocerSettings.getInstance()
        val default = KDocTemplate.DEFAULT

        val template = KDocTemplate(
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
            existingKDocPolicy = sheet?.existingKDocPolicy ?: settings.existingKDocPolicy,
        )
    }

    /** YAML value wins; then a non-blank settings override; finally the built-in default. */
    private fun pick(fromSheet: String?, fromSettings: String, default: String): String =
        fromSheet ?: fromSettings.ifBlank { default }
}
