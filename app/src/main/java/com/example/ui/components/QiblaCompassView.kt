package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.QiblaCalculator
import kotlin.math.*

@Composable
fun QiblaCompassView(
    deviceHeading: Float,
    qiblaBearing: Double,
    hasCompassSensor: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAligned = QiblaCalculator.isAligned(deviceHeading, qiblaBearing)

    // Trigger haptic vibration once when alignment is achieved
    var wasAligned by remember { mutableStateOf(false) }
    LaunchedEffect(isAligned) {
        if (isAligned && !wasAligned) {
            triggerHapticFeedback(context)
        }
        wasAligned = isAligned
    }

    // Smooth needle animation
    val animatedHeading by animateFloatAsState(
        targetValue = deviceHeading,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "compass_heading"
    )

    val relativeQiblaAngle = (qiblaBearing.toFloat() - animatedHeading + 360f) % 360f

    val glowColor by animateColorAsState(
        targetValue = if (isAligned) QiblaGreenAligned else IslamicGoldSecondary,
        animationSpec = tween(400),
        label = "glow_color"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("qibla_compass_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Alignment Status Banner
        Surface(
            color = if (isAligned) IslamicGreenContainer else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isAligned) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = IslamicGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "أنت الآن في اتجاه القبلة مباشرة! (تقبل الله)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenOnContainer,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "قم بتدوير الهاتف حتى يتطابق المؤشر الذهبي مع السهم الأخضر",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Animated Canvas Compass
        Box(
            modifier = Modifier
                .size(270.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        )
                    )
                )
                .border(3.dp, glowColor, CircleShape)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("compass_canvas")
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f

                // Draw outer degree marks & ring
                drawCompassDial(center, radius, animatedHeading)

                // Draw Qibla Pointer towards Kaaba relative to phone heading
                drawQiblaIndicator(center, radius, relativeQiblaAngle, isAligned)

                // Center hub
                drawCircle(
                    color = if (isAligned) QiblaGreenAligned else IslamicGoldSecondary,
                    radius = 9.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = center
                )
            }
        }

        // Telemetry Readings (Qibla Bearing, Phone Heading, Deviation)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoMetricBadge(
                label = "زاوية القبلة",
                value = "${qiblaBearing.roundToInt()}°",
                subtext = "من الشمال الجغرافي"
            )

            InfoMetricBadge(
                label = "اتجاه هاتفك",
                value = "${deviceHeading.roundToInt()}°",
                subtext = if (hasCompassSensor) "قراءة البوصلة" else "افتراضي"
            )

            val diff = QiblaCalculator.calculateAngleDifference(deviceHeading, qiblaBearing).roundToInt()
            InfoMetricBadge(
                label = "الانحراف",
                value = if (diff == 0) "0°" else "${abs(diff)}° ${if (diff > 0) "يسار" else "يمين"}",
                subtext = if (isAligned) "متطابق تماماً" else "حرك هاتفك"
            )
        }
    }
}

@Composable
private fun InfoMetricBadge(
    label: String,
    value: String,
    subtext: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = subtext,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
    }
}

private fun DrawScope.drawCompassDial(center: Offset, radius: Float, heading: Float) {
    // Rotating bezel according to device heading
    rotate(-heading, pivot = center) {
        // Outer rim
        drawCircle(
            color = Color(0x33888888),
            radius = radius - 4.dp.toPx(),
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
        )

        // Draw 36 tick marks (every 10 degrees)
        for (i in 0 until 36) {
            val angleDeg = i * 10f
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val isMajor = i % 9 == 0
            val tickLen = if (isMajor) 12.dp.toPx() else 6.dp.toPx()
            val strokeW = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx()
            val tickColor = if (isMajor) IslamicGreenPrimary else Color(0x88888888)

            val startX = center.x + (radius - 8.dp.toPx() - tickLen) * sin(angleRad).toFloat()
            val startY = center.y - (radius - 8.dp.toPx() - tickLen) * cos(angleRad).toFloat()
            val endX = center.x + (radius - 8.dp.toPx()) * sin(angleRad).toFloat()
            val endY = center.y - (radius - 8.dp.toPx()) * cos(angleRad).toFloat()

            drawLine(
                color = tickColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }

        // North Marker (شمال)
        val northPath = Path().apply {
            moveTo(center.x, center.y - radius + 22.dp.toPx())
            lineTo(center.x - 7.dp.toPx(), center.y - radius + 34.dp.toPx())
            lineTo(center.x + 7.dp.toPx(), center.y - radius + 34.dp.toPx())
            close()
        }
        drawPath(northPath, color = Color(0xFFDC2626)) // Distinct North Red arrow
    }
}

private fun DrawScope.drawQiblaIndicator(
    center: Offset,
    radius: Float,
    relativeAngle: Float,
    isAligned: Boolean
) {
    rotate(relativeAngle, pivot = center) {
        val needleLength = radius - 24.dp.toPx()
        val needleColor = if (isAligned) QiblaGreenAligned else QiblaGoldNeedle

        // Kaaba Qibla Pointer Needle
        val needlePath = Path().apply {
            moveTo(center.x, center.y - needleLength)
            lineTo(center.x - 9.dp.toPx(), center.y - 12.dp.toPx())
            lineTo(center.x + 9.dp.toPx(), center.y - 12.dp.toPx())
            close()
        }
        drawPath(needlePath, color = needleColor)

        // Opposing tail
        val tailPath = Path().apply {
            moveTo(center.x, center.y + needleLength * 0.45f)
            lineTo(center.x - 6.dp.toPx(), center.y + 10.dp.toPx())
            lineTo(center.x + 6.dp.toPx(), center.y + 10.dp.toPx())
            close()
        }
        drawPath(tailPath, color = Color(0x66888888))

        // Kaaba Icon Cube at the tip of the needle
        val cubeSize = 18.dp.toPx()
        val cubeTop = center.y - needleLength - cubeSize * 0.6f
        val cubeLeft = center.x - cubeSize / 2f

        // Black Kaaba cube body
        drawRoundRect(
            color = Color(0xFF1A1A1A),
            topLeft = Offset(cubeLeft, cubeTop),
            size = androidx.compose.ui.geometry.Size(cubeSize, cubeSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
        )
        // Golden Kiswa band on the Kaaba
        drawLine(
            color = IslamicGoldSecondary,
            start = Offset(cubeLeft, cubeTop + cubeSize * 0.35f),
            end = Offset(cubeLeft + cubeSize, cubeTop + cubeSize * 0.35f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

private fun triggerHapticFeedback(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(80)
        }
    } catch (_: Exception) {
        // Ignore if device has no vibrator
    }
}
