package com.example

import android.app.Application
import com.example.notification.NotificationHelper
import com.example.util.PreferenceManager

class HijriQiblaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)

        val prefs = PreferenceManager(this)
        if (prefs.isNotificationEnabled) {
            NotificationHelper.updateNotification(this)
        }
    }
}
