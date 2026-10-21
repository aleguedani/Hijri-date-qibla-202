package com.example.model

/**
 * Represents the nearest approaching Islamic occasion among the 3 designated events:
 * 1. شهر رمضان (1 رمضان)
 * 2. عيد الأضحى (10 ذو الحجة)
 * 3. شهر محرم (1 محرم - رأس السنة الهجرية)
 */
data class ApproachingOccasion(
    val nameAr: String,
    val remainingDays: Int,
    val formattedCountdown: String,
    val isOngoingNow: Boolean = false
)
