package com.kdocer.service

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
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
            return ServiceManager.getService(KDocerSettings::class.java)
        }
    }

    var isAllowedPrivate: Boolean = false
    var isAllowedPublic: Boolean = true
    var isAllowedProtected: Boolean = true
    var isAllowedInternal: Boolean = true

    var isAllowedKeepDoc: Boolean = false
    var isAllowedReplaceDoc: Boolean = true

    var isAllowedOverride: Boolean = false
    var isSplittedClassNames: Boolean = true

    var isAllowedClass: Boolean = true
    var isAllowedFun: Boolean = true
    var isAllowedField: Boolean = false

    var lastShowedTime: Long = 0L
    var isDisabledNotification: Boolean = false

    var isAllowedEmptyConstructor: Boolean = true  // for backward compatible behavior

    override fun getState(): KDocerSettings? {
        return this
    }

    override fun loadState(state: KDocerSettings) {
        XmlSerializerUtil.copyBean(state, this);
    }
}