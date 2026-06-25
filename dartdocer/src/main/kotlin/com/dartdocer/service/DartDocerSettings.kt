package com.dartdocer.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.docer.engine.merge.ExistingDocPolicy

@State(
    name = "dartdocer_configuration",
    storages = [Storage(file = "dartdocer.xml")]
)
class DartDocerSettings : PersistentStateComponent<DartDocerSettings> {

    companion object {
        fun getInstance(): DartDocerSettings {
            return ApplicationManager.getApplication().getService(DartDocerSettings::class.java)
        }
    }

    // Visibility (Dart: public vs underscore-prefixed private)
    var isAllowedPublic: Boolean = true
    var isAllowedPrivate: Boolean = false

    // Element types
    var isAllowedClass: Boolean = true
    var isAllowedFunction: Boolean = true
    var isAllowedField: Boolean = false
    var isAllowedExtension: Boolean = true
    var isAllowedMixin: Boolean = true

    // Behavior
    var isAllowedOverride: Boolean = false
    var isAppendName: Boolean = true
    var isSplitNames: Boolean = true

    // Existing doc policy
    var existingDocPolicy: ExistingDocPolicy = ExistingDocPolicy.MERGE

    // Templates
    var templateFunctionDescription: String = ""
    var templateReturn: String = ""
    var templateClassDescription: String = ""
    var templateFieldDescription: String = ""
    var templateConstructor: String = ""

    // Features
    var isFlutterAware: Boolean = true
    var isUsageExample: Boolean = false
    var isConstructorLine: Boolean = true
    var isThrowsDetection: Boolean = true
    var isSinceTag: Boolean = false
    var sinceVersion: String = ""
    var isSeeReferences: Boolean = false

    // Notifications
    var isDisabledNotification: Boolean = false
    var actionCount: Int = 0

    override fun getState(): DartDocerSettings? = this

    override fun loadState(state: DartDocerSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
