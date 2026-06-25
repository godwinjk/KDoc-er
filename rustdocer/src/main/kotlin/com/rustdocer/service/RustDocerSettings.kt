package com.rustdocer.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.docer.engine.merge.ExistingDocPolicy

@State(
    name = "rustdocer_configuration",
    storages = [Storage(file = "rustdocer.xml")]
)
class RustDocerSettings : PersistentStateComponent<RustDocerSettings> {

    companion object {
        fun getInstance(): RustDocerSettings {
            return ApplicationManager.getApplication().getService(RustDocerSettings::class.java)
        }
    }

    // Visibility
    var isAllowedPub: Boolean = true
    var isAllowedPubCrate: Boolean = true
    var isAllowedPubSuper: Boolean = true
    var isAllowedPrivate: Boolean = false

    // Element types
    var isAllowedFunction: Boolean = true
    var isAllowedStruct: Boolean = true
    var isAllowedEnum: Boolean = true
    var isAllowedTrait: Boolean = true
    var isAllowedImpl: Boolean = true
    var isAllowedModule: Boolean = false
    var isAllowedTypeAlias: Boolean = true
    var isAllowedConst: Boolean = false

    // Behavior
    var isAppendName: Boolean = true
    var isSplitNames: Boolean = true

    // Existing doc policy
    var existingDocPolicy: ExistingDocPolicy = ExistingDocPolicy.MERGE

    // Templates
    var templateFunctionDescription: String = ""
    var templateReturn: String = ""
    var templateStructDescription: String = ""
    var templateFieldDescription: String = ""
    var templateParam: String = ""

    // Features
    var isPanicDetection: Boolean = true
    var isUnsafeSafetySection: Boolean = true
    var isErrorsSection: Boolean = true
    var isExamplesSection: Boolean = false
    var isFrameworkAware: Boolean = true
    var isUsageExample: Boolean = false
    var isSinceTag: Boolean = false
    var sinceVersion: String = ""
    var isSeeReferences: Boolean = false

    // Notifications
    var isDisabledNotification: Boolean = false
    var actionCount: Int = 0

    override fun getState(): RustDocerSettings? = this

    override fun loadState(state: RustDocerSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
