package com.kdocer.style

import com.kdocer.merge.ExistingKDocPolicy
import com.kdocer.template.KDocTemplate

/**
 * The fully resolved configuration consumed by the generators — the single source of
 * truth produced by [StyleLoader] after merging `.kdocer.yaml`, the settings panel and
 * built-in defaults.
 */
data class ResolvedStyle(
    val appendName: Boolean,
    val splitNames: Boolean,
    val verbMapping: Map<String, String>,
    val frameworkAware: Boolean,
    val aspectNotes: Map<String, String>,
    val template: KDocTemplate,
    val usageExample: Boolean,
    val includeConstructor: Boolean,
    val existingKDocPolicy: ExistingKDocPolicy,
)
