package com.rustdocer.util

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.rustdocer.service.RustDocerSettings

object NotificationHelper {
    private const val GROUP_ID = "RustDoc-er"
    private const val ACTION_THRESHOLD = 20

    fun showNotification(message: String) {
        if (!checkIsAllowedSlot()) return
        notify("Like it", message)
    }

    fun showNotification(title: String, message: String) {
        notify(title, message)
    }

    private fun notify(title: String, message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(title, message, NotificationType.INFORMATION)
            .notify(null)
    }

    private fun checkIsAllowedSlot(): Boolean {
        val settings = RustDocerSettings.getInstance()
        if (settings.isDisabledNotification) return false

        settings.actionCount++
        if (settings.actionCount >= ACTION_THRESHOLD) {
            settings.actionCount = 0
            return true
        }
        return false
    }
}
