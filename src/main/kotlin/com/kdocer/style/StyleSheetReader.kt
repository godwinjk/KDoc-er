package com.kdocer.style

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.kdocer.merge.ExistingKDocPolicy
import org.yaml.snakeyaml.Yaml
import java.io.File

/**
 * Reads and parses the optional `.kdocer.yaml` file from a project's root directory.
 *
 * The file is entirely optional: any read or parse failure (missing file, malformed
 * YAML, SnakeYAML not on the classpath) resolves to `null`, and callers fall back to
 * the settings panel / built-in defaults.
 */
object StyleSheetReader {

    private const val FILE_NAME = ".kdocer.yaml"
    private val LOG = logger<StyleSheetReader>()

    fun read(project: Project): StyleSheet? {
        val base = project.basePath ?: return null
        val file = File(base, FILE_NAME)
        if (!file.exists() || !file.isFile) return null
        return try {
            @Suppress("UNCHECKED_CAST")
            val root = Yaml().load(file.readText()) as? Map<String, Any?> ?: return null
            parse(root)
        } catch (t: Throwable) {
            LOG.warn("Failed to parse $FILE_NAME; falling back to settings/defaults", t)
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parse(root: Map<String, Any?>): StyleSheet {
        val style = root["style"] as? Map<String, Any?> ?: emptyMap()
        val templates = root["templates"] as? Map<String, Any?> ?: emptyMap()
        val function = templates["function"] as? Map<String, Any?> ?: emptyMap()
        val klass = templates["class"] as? Map<String, Any?> ?: emptyMap()
        val property = templates["property"] as? Map<String, Any?> ?: emptyMap()
        val aspects = root["aspects"] as? Map<String, Any?> ?: emptyMap()

        val verbMapping = (style["verbMapping"] as? Map<String, Any?>)
            ?.mapNotNull { (k, v) -> (v as? String)?.let { k to it } }
            ?.toMap() ?: emptyMap()

        val aspectNotes = (aspects["notes"] as? Map<String, Any?>)
            ?.mapNotNull { (k, v) -> (v as? String)?.let { k to it } }
            ?.toMap() ?: emptyMap()

        val existingKDocPolicy = (style["existingKDoc"] as? String)?.let { value ->
            ExistingKDocPolicy.entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
        }

        return StyleSheet(
            appendName = style["appendName"] as? Boolean,
            splitNames = style["splitNames"] as? Boolean,
            verbMapping = verbMapping,
            functionDescription = function["description"] as? String,
            paramLine = function["param"] as? String,
            returnLine = function["return"] as? String,
            classDescription = klass["description"] as? String,
            constructorLine = klass["constructor"] as? String,
            propertyDescription = property["description"] as? String,
            frameworkAware = style["frameworkAware"] as? Boolean,
            aspectNotes = aspectNotes,
            usageExample = style["usageExample"] as? Boolean,
            includeConstructor = style["includeConstructor"] as? Boolean,
            existingKDocPolicy = existingKDocPolicy,
        )
    }
}
