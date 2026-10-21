package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.notification.NotificationHelper
import com.example.util.PreferenceManager

/**
 * BroadcastReceiver triggered at midnight to update the ongoing daily Hijri date notification.
 */
class DailyDateUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = PreferenceManager(context)
        if (prefs.isNotificationEnabled && prefs.notificationMode == PreferenceManager.MODE_DAILY) {
            NotificationHelper.updateNotification(context)
        }
    }
}
