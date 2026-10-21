package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.model.HijriDate
import com.example.receiver.DailyDateUpdateReceiver
import com.example.util.HijriCalendarHelper
import com.example.util.PreferenceManager
import java.util.Calendar

/**
 * Handles creation, updating, and scheduling of the discreet ongoing Hijri Date notification.
 * Configured with IMPORTANCE_MIN and transparent icon to appear strictly in the pulled-down
 * notification shade WITHOUT showing an icon or symbol next to the clock in the top status bar.
 */
object NotificationHelper {

    const val CHANNEL_ID = "hijri_date_shade_channel_v3"
    const val NOTIFICATION_ID = 1001
    private const val ALARM_REQUEST_CODE = 2001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = "عرض التاريخ الهجري داخل لوحة الإشعارات فقط دون أيقونة في شريط الحالة"
            // IMPORTANCE_MIN specifically suppresses status bar icons and sound, keeping the item
            // strictly in the expanded notification shade
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Clean up previous channel version if present
            try {
                notificationManager.deleteNotificationChannel("hijri_date_channel")
                notificationManager.deleteNotificationChannel("hijri_date_shade_channel_v2")
            } catch (_: Exception) {}

            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Updates or creates the persistent Hijri notification based on current preferences.
     */
    fun updateNotification(context: Context, explicitHijriDate: HijriDate? = null) {
        val prefs = PreferenceManager(context)
        if (!prefs.isNotificationEnabled) {
            cancelNotification(context)
            return
        }

        createNotificationChannel(context)

        val hijriDate = if (prefs.notificationMode == PreferenceManager.MODE_DAILY || explicitHijriDate == null) {
            HijriCalendarHelper.getTodayHijri(prefs.adjustmentDays)
        } else {
            explicitHijriDate
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            pendingIntentFlags
        )

        // Clear, simple single-line format requested: «التاريخ الهجري: 16/10/1463 هـ»
        val title = "التاريخ الهجري: ${hijriDate.day}/${hijriDate.month}/${hijriDate.year} هـ"
        val approaching = HijriCalendarHelper.getNearestApproachingOccasion(hijriDate)
        val approachingStr = if (approaching != null) " • ${approaching.formattedCountdown}" else ""
        val subtitle = "${hijriDate.dayOfWeekName}، ${hijriDate.day} ${hijriDate.monthName} ${hijriDate.year} هـ$approachingStr"

        // Build compact notification with PRIORITY_MIN and transparent small icon
        // so NO icon appears in the status bar next to the clock
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_silent) // Transparent icon
            .setContentTitle(title)
            .setContentText(subtitle)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_MIN) // Min priority for pre-Oreo & Android status bar silence
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
            // Handled when POST_NOTIFICATIONS is not yet granted on Android 13+
        }

        // Schedule next midnight update if in daily mode
        if (prefs.notificationMode == PreferenceManager.MODE_DAILY) {
            scheduleMidnightUpdate(context)
        } else {
            cancelMidnightUpdate(context)
        }
    }

    fun cancelNotification(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_ID)
        cancelMidnightUpdate(context)
    }

    /**
     * Schedules an alarm to update the notification automatically at midnight.
     */
    fun scheduleMidnightUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyDateUpdateReceiver::class.java)

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            pendingIntentFlags
        )

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 5)
            set(Calendar.MILLISECOND, 0)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun cancelMidnightUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyDateUpdateReceiver::class.java)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            pendingIntentFlags
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
