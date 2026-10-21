package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.HijriDate
import com.example.notification.NotificationHelper
import com.example.ui.components.DateInputField
import com.example.ui.components.HijriResultCard
import com.example.ui.components.IslamicHeader
import com.example.ui.components.NotificationControlCard
import com.example.util.HijriCalendarHelper
import com.example.util.PreferenceManager

@Composable
fun HijriConverterScreen(
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }

    val lastSaved = remember { prefs.getLastDate() }
    var inputDay by remember { mutableIntStateOf(lastSaved.first) }
    var inputMonth by remember { mutableIntStateOf(lastSaved.second) }
    var inputYear by remember { mutableIntStateOf(lastSaved.third) }

    var adjustmentDays by remember { mutableIntStateOf(prefs.adjustmentDays) }
    var isNotificationEnabled by remember { mutableStateOf(prefs.isNotificationEnabled) }
    var notificationMode by remember { mutableStateOf(prefs.notificationMode) }

    // Calculated Hijri Date state
    var currentHijriDate by remember {
        mutableStateOf(
            HijriCalendarHelper.convertGregorianToHijri(
                inputYear,
                inputMonth,
                inputDay,
                adjustmentDays
            )
        )
    }

    fun recalculateAndSync(d: Int, m: Int, y: Int, adj: Int) {
        val result = HijriCalendarHelper.convertGregorianToHijri(y, m, d, adj)
        currentHijriDate = result
        prefs.saveLastDate(d, m, y)
        prefs.adjustmentDays = adj

        if (isNotificationEnabled) {
            NotificationHelper.updateNotification(context, result)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("hijri_converter_screen")
    ) {
        // Islamic Banner Header with countdown
        IslamicHeader(currentHijriDate = currentHijriDate)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Gregorian Date Input Card
            DateInputField(
                day = inputDay,
                month = inputMonth,
                year = inputYear,
                onDateChanged = { d, m, y ->
                    inputDay = d
                    inputMonth = m
                    inputYear = y
                },
                onConvertClicked = {
                    recalculateAndSync(inputDay, inputMonth, inputYear, adjustmentDays)
                }
            )

            // Converted Hijri Date Result Card with Lunar Adjustments
            HijriResultCard(
                hijriDate = currentHijriDate,
                adjustmentDays = adjustmentDays,
                onAdjustmentChanged = { newAdj ->
                    adjustmentDays = newAdj
                    recalculateAndSync(inputDay, inputMonth, inputYear, newAdj)
                }
            )

            // Persistent Notification Controller Card
            NotificationControlCard(
                isEnabled = isNotificationEnabled,
                notificationMode = notificationMode,
                currentDate = currentHijriDate,
                hasNotificationPermission = hasNotificationPermission,
                onRequestPermission = onRequestNotificationPermission,
                onToggleEnabled = { enabled ->
                    isNotificationEnabled = enabled
                    prefs.isNotificationEnabled = enabled
                    if (enabled) {
                        NotificationHelper.updateNotification(context, currentHijriDate)
                    } else {
                        NotificationHelper.cancelNotification(context)
                    }
                },
                onModeChanged = { mode ->
                    notificationMode = mode
                    prefs.notificationMode = mode
                    if (isNotificationEnabled) {
                        NotificationHelper.updateNotification(context, currentHijriDate)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
