package com.kdocer.merge

/**
 * What to do when a declaration already has a KDoc.
 *
 * - [MERGE]: keep the user's summary and existing tag text, only add missing tags
 *   (Eclipse/NetBeans style) — the default, never destroys hand-written content.
 * - [KEEP]: leave an existing KDoc completely untouched.
 * - [REPLACE]: regenerate the whole KDoc, discarding edits.
 */
enum class ExistingKDocPolicy {
    MERGE,
    KEEP,
    REPLACE,
}
