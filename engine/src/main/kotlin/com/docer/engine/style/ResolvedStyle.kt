package com.docer.engine.style

import com.docer.engine.merge.ExistingDocPolicy
import com.docer.engine.template.DocTemplate

data class ResolvedStyle(
    val appendName: Boolean,
    val splitNames: Boolean,
    val verbMapping: Map<String, String>,
    val frameworkAware: Boolean,
    val aspectNotes: Map<String, String>,
    val template: DocTemplate,
    val usageExample: Boolean,
    val includeConstructor: Boolean,
    val existingDocPolicy: ExistingDocPolicy,
    val throwsDetection: Boolean,
    val sinceTag: Boolean,
    val sinceVersion: String,
    val seeReferences: Boolean,
)
