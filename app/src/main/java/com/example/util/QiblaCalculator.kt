package com.example.util

import kotlin.math.*

/**
 * Spherical trigonometry calculator for True Qibla Bearing and distance to the Holy Kaaba.
 */
object QiblaCalculator {

    // Holy Kaaba coordinates in Mecca
    const val MECCA_LATITUDE = 21.422487
    const val MECCA_LONGITUDE = 39.826206
    const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculates the Qibla bearing in degrees from True North (0°..360° clockwise).
     */
    fun calculateQiblaBearing(userLat: Double, userLng: Double): Double {
        val lat1 = Math.toRadians(userLat)
        val lat2 = Math.toRadians(MECCA_LATITUDE)
        val dLng = Math.toRadians(MECCA_LONGITUDE - userLng)

        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)

        var bearing = Math.toDegrees(atan2(y, x))
        bearing = (bearing + 360.0) % 360.0
        return bearing
    }

    /**
     * Calculates great-circle distance in kilometers to the Holy Kaaba using Haversine formula.
     */
    fun calculateDistanceToKaabaKm(userLat: Double, userLng: Double): Double {
        val lat1 = Math.toRadians(userLat)
        val lat2 = Math.toRadians(MECCA_LATITUDE)
        val dLat = Math.toRadians(MECCA_LATITUDE - userLat)
        val dLng = Math.toRadians(MECCA_LONGITUDE - userLng)

        val a = sin(dLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Calculates the smallest angular difference between phone heading and Qibla (-180°..+180°).
     * 0° means the phone is pointing directly at the Kaaba.
     */
    fun calculateAngleDifference(deviceHeading: Float, qiblaBearing: Double): Float {
        var diff = (qiblaBearing.toFloat() - deviceHeading) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return diff
    }

    /**
     * Determines whether phone is aligned with Qibla within a strict tolerance (e.g. ±3 degrees).
     */
    fun isAligned(deviceHeading: Float, qiblaBearing: Double, toleranceDegrees: Float = 3.5f): Boolean {
        val diff = abs(calculateAngleDifference(deviceHeading, qiblaBearing))
        return diff <= toleranceDegrees
    }
}
