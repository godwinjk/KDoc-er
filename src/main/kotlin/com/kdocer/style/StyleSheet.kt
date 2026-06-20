package com.kdocer.style

import com.kdocer.merge.ExistingKDocPolicy

/**
 * In-memory representation of an optional `.kdocer.yaml` style sheet. Every field is
 * nullable so that "not specified in the file" is distinct from "explicitly set",
 * letting [StyleLoader] fall back to settings/defaults on a per-field basis.
 */
data class StyleSheet(
    val appendName: Boolean? = null,
    val splitNames: Boolean? = null,
    val verbMapping: Map<String, String> = emptyMap(),

    val functionDescription: String? = null,
    val paramLine: String? = null,
    val returnLine: String? = null,
    val classDescription: String? = null,
    val propertyDescription: String? = null,
    val constructorLine: String? = null,

    val frameworkAware: Boolean? = null,
    val aspectNotes: Map<String, String> = emptyMap(),

    val usageExample: Boolean? = null,
    val includeConstructor: Boolean? = null,
    val existingKDocPolicy: ExistingKDocPolicy? = null,
)
