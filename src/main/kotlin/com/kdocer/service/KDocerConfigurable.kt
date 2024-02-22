package com.kdocer.service

import com.intellij.openapi.options.Configurable
import com.kdocer.SettingsPanel
import com.kdocer.util.Constants
import javax.swing.JComponent

/**
 * Created by Godwin on 8/1/2020 2:36 PM.
 *
 * @author : Godwin Joseph Kurinjikattu
 * @since : 2020
 */
class KDocerConfigurable : Configurable {

    private val callback = SettingsPanel.ConfigCallback {
        time = System.currentTimeMillis()
    }
    var time = 0L

    private lateinit var componet: SettingsPanel
    override fun isModified(): Boolean {
        val settings: KDocerSettings = KDocerSettings.getInstance()
        return settings.isAllowedOverride != componet.isAllowedOverride ||
                settings.isAllowedPrivate != componet.isAllowedPrivate ||
                settings.isAllowedPublic != componet.isAllowedPublic ||
                settings.isAllowedProtected != componet.isAllowedProtected ||
                settings.isAllowedInternal != componet.isAllowedInternal ||
                settings.isAllowedClass != componet.isAllowedClass ||
                settings.isAllowedField != componet.isAllowedField ||
                settings.isAllowedFun != componet.isAllowedFun ||
                settings.isSplittedClassNames != componet.isSplittedClassNames ||
                settings.isAppendName != componet.isAppendName ||
                settings.isAllowedKeepDoc != componet.isAllowedKeepDoc ||
                settings.isAllowedReplaceDoc != componet.isAllowedReplaceDoc ||
                settings.isDisabledNotification != componet.isDisabledNotification
    }

    override fun getDisplayName(): String {
        return Constants.SETTINGS_DISP_NAME
    }

    override fun apply() {
        val settings: KDocerSettings = KDocerSettings.getInstance()

        settings.isAllowedOverride = componet.isAllowedOverride
        settings.isAllowedPrivate = componet.isAllowedPrivate
        settings.isAllowedPublic = componet.isAllowedPublic
        settings.isAllowedProtected = componet.isAllowedProtected
        settings.isAllowedInternal = componet.isAllowedInternal
        settings.isAllowedClass = componet.isAllowedClass
        settings.isAllowedField = componet.isAllowedField
        settings.isAllowedFun = componet.isAllowedFun
        settings.isSplittedClassNames = componet.isSplittedClassNames
        settings.isAppendName = componet.isAppendName
        settings.isAllowedKeepDoc = componet.isAllowedKeepDoc
        settings.isAllowedReplaceDoc = componet.isAllowedReplaceDoc
        settings.isDisabledNotification = componet.isDisabledNotification

//        if (settings.isDisabledNotification) {
//            settings.lastShowedTime = time
//            NotificationHelper.showNotification("Warning", "Disabling notification only valid up to one week")
//        }
    }

    override fun reset() {
        super.reset()
        val settings: KDocerSettings = KDocerSettings.getInstance()

        componet.isAllowedOverride = settings.isAllowedOverride
        componet.isAllowedPrivate = settings.isAllowedPrivate
        componet.isAllowedPublic = settings.isAllowedPublic
        componet.isAllowedProtected = settings.isAllowedProtected
        componet.isAllowedInternal = settings.isAllowedInternal
        componet.isAllowedClass = settings.isAllowedClass
        componet.isAllowedField = settings.isAllowedField
        componet.isAllowedFun = settings.isAllowedFun
        componet.isSplittedClassNames = settings.isSplittedClassNames
        componet.isAppendName = settings.isAppendName
        componet.isAllowedKeepDoc = settings.isAllowedKeepDoc
        componet.isAllowedReplaceDoc = settings.isAllowedReplaceDoc
        componet.isDisabledNotification = settings.isDisabledNotification

//        if (settings.isDisabledNotification) {
//            val lastDisabledTime = settings.lastShowedTime
//            if (lastDisabledTime < (System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7)) {
//                componet.isDisabledNotification = false
//                apply()
//            }
//        }
    }

    override fun createComponent(): JComponent? {
        componet = SettingsPanel()
        componet.callback = callback
        return componet.panel
    }

    override fun disposeUIResources() {
        super.disposeUIResources()
    }
}