package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import com.example.notification.NotificationHelper
import com.example.ui.screens.HijriConverterScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.PreferenceManager

enum class AppTab(val titleAr: String) {
    HIJRI("التاريخ الهجري"),
    QIBLA("اتجاه القبلة"),
    SETTINGS("الإعدادات")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTab by remember { mutableStateOf(AppTab.HIJRI) }

    // Track notification permission state
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            val prefs = PreferenceManager(context)
            if (prefs.isNotificationEnabled) {
                NotificationHelper.updateNotification(context)
            }
        }
    }

    // Auto-request notification permission on Android 13+ on first launch if enabled
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Handle system back navigation: pop to Hijri home tab if on other tabs
    BackHandler(enabled = selectedTab != AppTab.HIJRI) {
        selectedTab = AppTab.HIJRI
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                // Tab 1: Hijri Date
                NavigationBarItem(
                    selected = selectedTab == AppTab.HIJRI,
                    onClick = { selectedTab = AppTab.HIJRI },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.HIJRI) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "التاريخ الهجري"
                        )
                    },
                    label = {
                        Text(
                            text = AppTab.HIJRI.titleAr,
                            fontWeight = if (selectedTab == AppTab.HIJRI) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_item_hijri")
                )

                // Tab 2: Qibla Compass
                NavigationBarItem(
                    selected = selectedTab == AppTab.QIBLA,
                    onClick = { selectedTab = AppTab.QIBLA },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.QIBLA) Icons.Default.Explore else Icons.Outlined.Explore,
                            contentDescription = "اتجاه القبلة"
                        )
                    },
                    label = {
                        Text(
                            text = AppTab.QIBLA.titleAr,
                            fontWeight = if (selectedTab == AppTab.QIBLA) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_item_qibla")
                )

                // Tab 3: Settings
                NavigationBarItem(
                    selected = selectedTab == AppTab.SETTINGS,
                    onClick = { selectedTab = AppTab.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.SETTINGS) Icons.Default.Settings else Icons.Outlined.Settings,
                            contentDescription = "الإعدادات"
                        )
                    },
                    label = {
                        Text(
                            text = AppTab.SETTINGS.titleAr,
                            fontWeight = if (selectedTab == AppTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.padding(innerPadding),
            label = "tab_content_transition"
        ) { tab ->
            when (tab) {
                AppTab.HIJRI -> HijriConverterScreen(
                    hasNotificationPermission = hasNotificationPermission,
                    onRequestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
                AppTab.QIBLA -> QiblaScreen()
                AppTab.SETTINGS -> SettingsScreen()
            }
        }
    }
}
