package com.tanno.assistant

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class TannoNotificationListener : NotificationListenerService() {

    companion object {
        var lastSender: String? = null
        var lastMessage: String? = null
        var lastApp: String? = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn ?: return
        val extras = notification.notification.extras

        val appLabel = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(notification.packageName, 0)
            ).toString()
        } catch (e: Exception) {
            notification.packageName
        }

        if (notification.packageName == packageName) return

        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        lastApp = appLabel
        lastSender = title
        lastMessage = text

        CommandRouter.speak(
            applicationContext,
            "$appLabel par $title ka naya message aaya: $text"
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
    }
}
