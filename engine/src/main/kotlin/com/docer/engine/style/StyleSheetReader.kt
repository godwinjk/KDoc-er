package com.docer.engine.style

import com.docer.engine.merge.ExistingDocPolicy
import org.yaml.snakeyaml.Yaml
import java.io.File

object StyleSheetReader {

    fun read(basePath: String, fileName: String = ".kdocer.yaml"): StyleSheet? {
        val file = File(basePath, fileName)
        if (!file.exists() || !file.isFile) return null
        return try {
            @Suppress("UNCHECKED_CAST")
            val root = Yaml().load(file.readText()) as? Map<String, Any?> ?: return null
            parse(root)
        } catch (_: Throwable) {
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

        val existingDocPolicy = (style["existingKDoc"] as? String)?.let { value ->
            ExistingDocPolicy.entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
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
            existingDocPolicy = existingDocPolicy,
            throwsDetection = style["throwsDetection"] as? Boolean,
            sinceTag = style["sinceTag"] as? Boolean,
            sinceVersion = style["sinceVersion"] as? String,
            seeReferences = style["seeReferences"] as? Boolean,
        )
    }
}
