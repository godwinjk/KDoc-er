package com.kdocer.util

import com.intellij.notification.Notification
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.kdocer.service.KDocerSettings

/**
 * Created by Godwin on 8/13/2020 8:30 PM.
 *
 * @author : Godwin Joseph Kurinjikattu
 * @since : 2020
 */

object NotificationHelper {
    private var lastShowedNotification: Long = 0L
    fun showNotification(message: String) {
        if (!checkIsAllowedSlot()) return
        Notifications.Bus.notify(
            Notification(
                "KDoc-er",
                "Like it",
                message,
                NotificationType.INFORMATION,
                NotificationListener.UrlOpeningListener(true)
            )
        )
    }

    fun showNotification(title: String, message: String) {
        Notifications.Bus.notify(
            Notification(
                "KDoc-er",
                title,
                message,
                NotificationType.INFORMATION,
                NotificationListener.UrlOpeningListener(true)
            )
        )
    }

    private fun checkIsAllowedSlot(): Boolean {
        val settings = KDocerSettings.getInstance()

        return if (settings.isDisabledNotification) false
        else if (lastShowedNotification > 0
            && (lastShowedNotification < (System.currentTimeMillis() - 1000 * 60 * 60 * 5))
        ) {//this means 5 hour difference
            lastShowedNotification = System.currentTimeMillis()
            true
        } else if (lastShowedNotification == 0L) {
            lastShowedNotification = System.currentTimeMillis()
            true
        } else false
    }
}