package com.kdocer.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.kdocer.merge.ExistingKDocPolicy
import com.kdocer.util.KDocerConfiguration

/**
 * Created by Godwin on 8/1/2020 1:39 PM.
 *
 * @author : Godwin Joseph Kurinjikattu
 * @since : 2020
 */
@State(
    name = KDocerConfiguration.CONFIGURATION_COMPONENT,
    storages = [Storage(
        file = "kdocer.xml"
    )]
)

class KDocerSettings : PersistentStateComponent<KDocerSettings> {
    companion object {
        fun getInstance(): KDocerSettings {
            return ApplicationManager.getApplication().getService(KDocerSettings::class.java)
        }
    }

    var isAllowedPrivate: Boolean = false
    var isAllowedPublic: Boolean = true
    var isAllowedProtected: Boolean = true
    var isAllowedInternal: Boolean = true

    // --- What to do when a declaration already has a KDoc ---
    var existingKDocPolicy: ExistingKDocPolicy = ExistingKDocPolicy.MERGE

    var isAllowedOverride: Boolean = false
    var isSplittedClassNames: Boolean = true
    var isAppendName: Boolean = true

    var isAllowedClass: Boolean = true
    var isAllowedFun: Boolean = true
    var isAllowedField: Boolean = false

    var lastShowedTime: Long = 0L
    var isDisabledNotification: Boolean = false
    var actionCount: Int = 0

    // --- Template overrides (empty string means "use built-in default") ---
    var templateFunctionDescription: String = ""
    var templateParam: String = ""
    var templateReturn: String = ""
    var templateClassDescription: String = ""
    var templatePropertyDescription: String = ""
    var templateConstructor: String = ""

    // --- Framework awareness (Compose / ViewModel / LiveData / data / sealed / object …) ---
    var isFrameworkAware: Boolean = true

    // --- Usage example (a small fenced sample call appended to function KDocs) ---
    var isUsageExample: Boolean = false

    // --- Whether to emit the @constructor line for classes ---
    var isConstructorLine: Boolean = true

    // --- @throws detection: scan function bodies for throw expressions ---
    var isThrowsDetection: Boolean = true

    // --- @since tag: stamp generated KDocs with a version ---
    var isSinceTag: Boolean = false
    var sinceVersion: String = ""

    // --- @see cross-references: link overrides to super, sealed subtypes to parent ---
    var isSeeReferences: Boolean = false

    override fun getState(): KDocerSettings? {
        return this
    }

    override fun loadState(state: KDocerSettings) {
        XmlSerializerUtil.copyBean(state, this);
    }
}