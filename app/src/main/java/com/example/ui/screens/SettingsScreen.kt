package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.IslamicHeader
import com.example.ui.theme.IslamicGoldSecondary

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen")
    ) {
        IslamicHeader()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "الإعدادات والمعلومات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Notifications & System Permissions Card
            SettingsInfoCard(
                icon = Icons.Default.NotificationsActive,
                title = "إعدادات الإشعارات وشريط الحالة",
                description = "• تم تصميم الإشعار ليعمل بوضع صامت ومخفي من شريط الحالة العلوي (دون أيقونة أو رمز بجانب الساعة)، ويظهر فقط عند سحب لوحة الإشعارات للأسفل.\n" +
                        "• لضمان استمرار ظهور التاريخ وتحديثه التلقائي، قد يتطلب الأمر في بعض الأجهزة (مثل شاومي، سامسونج، هواوي) استثناء التطبيق من موفر البطارية (Battery Optimization).",
                actionButtonText = "فتح إعدادات إشعارات التطبيق",
                onActionClicked = {
                    try {
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                        } else {
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(fallback)
                    }
                }
            )

            // Contact Developer Card (التواصل مع المطوّر)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_contact_developer")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "التواصل مع المطوّر",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    // Telegram Official Logo Icon Only (Clickable)
                    IconButton(
                        onClick = {
                            val username = "Aleguedani"
                            try {
                                val telegramIntent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=$username"))
                                context.startActivity(telegramIntent)
                            } catch (_: Exception) {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$username"))
                                context.startActivity(browserIntent)
                            }
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("btn_telegram_icon_only")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_telegram),
                            contentDescription = "تيليجرام",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }
            }

            // Offline Operations Card
            SettingsInfoCard(
                icon = Icons.Default.CloudOff,
                title = "العمل دون اتصال بالإنترنت",
                description = "• جميع عمليات التحويل بين التاريخ الميلادي والهجري تتم محلياً 100% دون الحاجة إلى أي اتصال بالإنترنت.\n" +
                        "• حساب زاوية القبلة والمسافة نحو الكعبة المشرفة يعتمد على معادلات المثلثات الكروية الدقيقة ويعمل بالكامل دون إنترنت بمجرد تحديد الموقع أو اختيار مدينتك."
            )

            // Astronomical & Hijri Calendar Note
            SettingsInfoCard(
                icon = Icons.Default.BrightnessMedium,
                title = "عن تقويم أم القرى ورؤية الهلال",
                description = "• يعتمد التطبيق على خوارزمية تقويم أم القرى الفلكية المعتمدة.\n" +
                        "• نظراً لأن بداية الأشهر الهجرية قد تختلف بيوم أو يومين بين الدول الإسلامية وفقاً لرؤية الهلال الشرعية، يمكنك استخدام ميزة «تصحيح رؤية الهلال (±1 أو ±2 يوم)» في شاشة التحويل لتوافق بلدك بدقة تامة."
            )

            // About the Application Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تطبيق التاريخ الهجري والقبلة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "الإصدار 1.0 • خفيف وسريع وبدون إعلانات",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "تطبيق إسلامي مخصص لخدمة المسلمين في كل مكان",
                        style = MaterialTheme.typography.labelSmall,
                        color = IslamicGoldSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsInfoCard(
    icon: ImageVector,
    title: String,
    description: String,
    actionButtonText: String? = null,
    onActionClicked: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (actionButtonText != null && onActionClicked != null) {
                OutlinedButton(
                    onClick = onActionClicked,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(actionButtonText, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
