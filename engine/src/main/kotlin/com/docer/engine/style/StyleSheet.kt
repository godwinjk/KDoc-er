package com.docer.engine.style

import com.docer.engine.merge.ExistingDocPolicy

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
    val existingDocPolicy: ExistingDocPolicy? = null,

    val throwsDetection: Boolean? = null,
    val sinceTag: Boolean? = null,
    val sinceVersion: String? = null,
    val seeReferences: Boolean? = null,
)
