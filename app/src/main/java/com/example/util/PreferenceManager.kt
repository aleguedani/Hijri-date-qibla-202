package com.example.util

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

/**
 * Manages persistent user preferences using SharedPreferences.
 */
class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "hijri_qibla_prefs"

        private const val KEY_LAST_DAY = "last_gregorian_day"
        private const val KEY_LAST_MONTH = "last_gregorian_month"
        private const val KEY_LAST_YEAR = "last_gregorian_year"

        private const val KEY_ADJUSTMENT = "hijri_adjustment"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_NOTIFICATION_MODE = "notification_mode" // "daily" or "fixed"

        private const val KEY_SELECTED_CITY_ID = "selected_city_id"
        private const val KEY_MANUAL_LAT = "manual_latitude"
        private const val KEY_MANUAL_LNG = "manual_longitude"
        private const val KEY_LOCATION_MODE = "location_mode" // "gps" or "city" or "manual"

        const val MODE_DAILY = "daily"
        const val MODE_FIXED = "fixed"
    }

    fun saveLastDate(day: Int, month: Int, year: Int) {
        prefs.edit()
            .putInt(KEY_LAST_DAY, day)
            .putInt(KEY_LAST_MONTH, month)
            .putInt(KEY_LAST_YEAR, year)
            .apply()
    }

    fun getLastDate(): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance()
        val defaultDay = cal.get(Calendar.DAY_OF_MONTH)
        val defaultMonth = cal.get(Calendar.MONTH) + 1
        val defaultYear = cal.get(Calendar.YEAR)

        val day = prefs.getInt(KEY_LAST_DAY, defaultDay)
        val month = prefs.getInt(KEY_LAST_MONTH, defaultMonth)
        val year = prefs.getInt(KEY_LAST_YEAR, defaultYear)
        return Triple(day, month, year)
    }

    var adjustmentDays: Int
        get() = prefs.getInt(KEY_ADJUSTMENT, 0)
        set(value) = prefs.edit().putInt(KEY_ADJUSTMENT, value.coerceIn(-2, 2)).apply()

    var isNotificationEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, value).apply()

    var notificationMode: String
        get() = prefs.getString(KEY_NOTIFICATION_MODE, MODE_DAILY) ?: MODE_DAILY
        set(value) = prefs.edit().putString(KEY_NOTIFICATION_MODE, value).apply()

    var selectedCityId: String
        get() = prefs.getString(KEY_SELECTED_CITY_ID, "cairo") ?: "cairo"
        set(value) = prefs.edit().putString(KEY_SELECTED_CITY_ID, value).apply()

    var manualLatitude: Double
        get() = Double.fromBits(prefs.getLong(KEY_MANUAL_LAT, 30.0444.toBits()))
        set(value) = prefs.edit().putLong(KEY_MANUAL_LAT, value.toBits()).apply()

    var manualLongitude: Double
        get() = Double.fromBits(prefs.getLong(KEY_MANUAL_LNG, 31.2357.toBits()))
        set(value) = prefs.edit().putLong(KEY_MANUAL_LNG, value.toBits()).apply()

    var locationMode: String
        get() = prefs.getString(KEY_LOCATION_MODE, "gps") ?: "gps"
        set(value) = prefs.edit().putString(KEY_LOCATION_MODE, value).apply()
}
