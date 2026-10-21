package com.example

import com.example.model.HijriDate
import com.example.util.HijriCalendarHelper
import com.example.util.QiblaCalculator
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.roundToInt

class ExampleUnitTest {

    @Test
    fun testGregorianToHijriConversion() {
        val hijriDate = HijriCalendarHelper.convertGregorianToHijri(2026, 10, 20, 0)
        assertTrue(hijriDate.day in 1..30)
        assertTrue(hijriDate.month in 1..12)
        assertEquals(1448, hijriDate.year)
        assertNotNull(hijriDate.monthName)
        assertNotNull(hijriDate.dayOfWeekName)
    }

    @Test
    fun testLunarAdjustment() {
        val baseDate = HijriCalendarHelper.convertGregorianToHijri(2026, 10, 20, 0)
        val plusOneDate = HijriCalendarHelper.convertGregorianToHijri(2026, 10, 20, 1)
        val minusOneDate = HijriCalendarHelper.convertGregorianToHijri(2026, 10, 20, -1)

        assertEquals(1, plusOneDate.adjustmentDays)
        assertEquals(-1, minusOneDate.adjustmentDays)
        assertNotEquals(baseDate.day, minusOneDate.day)
    }

    @Test
    fun testApproachingOccasionsWhenNear() {
        // Sha'ban 15, 1448 -> Ramadan is 14 or 15 days away (near!)
        val shabanDate = HijriDate(
            day = 15,
            month = 8,
            year = 1448,
            monthName = "شعبان",
            dayOfWeekName = "الجمعة",
            gregorianDay = 22,
            gregorianMonth = 1,
            gregorianYear = 2027
        )
        val nearOccasion = HijriCalendarHelper.getNearestApproachingOccasion(shabanDate)
        assertNotNull(nearOccasion)
        assertEquals("شهر رمضان", nearOccasion!!.nameAr)
        assertTrue(nearOccasion.remainingDays in 14..16)
        assertTrue(nearOccasion.formattedCountdown.contains("شهر رمضان"))
    }

    @Test
    fun testApproachingOccasionsWhenFar() {
        // Rabi' al-Thani 8, 1448 (Month 4) -> all 3 occasions (Ramadan, Eid al-Adha, Muharram) are > 90 days away!
        val rabiDate = HijriDate(
            day = 8,
            month = 4,
            year = 1448,
            monthName = "ربيع الآخر",
            dayOfWeekName = "الثلاثاء",
            gregorianDay = 20,
            gregorianMonth = 10,
            gregorianYear = 2026
        )
        val farOccasion = HijriCalendarHelper.getNearestApproachingOccasion(rabiDate, proximityThresholdDays = 90)
        // Must be null because none of the 3 is near
        assertNull(farOccasion)

        // Header countdown for Month 4 must also be null (since Ramadan is > 3 months away)
        val headerCountdown = HijriCalendarHelper.getHeaderCountdown(rabiDate)
        assertNull(headerCountdown)
    }

    @Test
    fun testHeaderCountdownBeforeRamadanWithin3Months() {
        // Sha'ban 15 (Month 8) -> Ramadan is ~14 days away (< 3 months)
        val shabanDate = HijriDate(
            day = 15,
            month = 8,
            year = 1448,
            monthName = "شعبان",
            dayOfWeekName = "الجمعة",
            gregorianDay = 22,
            gregorianMonth = 1,
            gregorianYear = 2027
        )
        val headerCountdown = HijriCalendarHelper.getHeaderCountdown(shabanDate)
        assertNotNull(headerCountdown)
        assertTrue(headerCountdown!!.startsWith("🌙 متبقي"))
        assertTrue(headerCountdown.contains("رمضان"))
    }

    @Test
    fun testHeaderCountdownAfterRamadanUntilEidAdha() {
        // Shawwal 10 (Month 10) -> After Ramadan, must count down to Eid al-Adha!
        val shawwalDate = HijriDate(
            day = 10,
            month = 10,
            year = 1448,
            monthName = "شوال",
            dayOfWeekName = "الجمعة",
            gregorianDay = 19,
            gregorianMonth = 3,
            gregorianYear = 2027
        )
        val headerCountdown = HijriCalendarHelper.getHeaderCountdown(shawwalDate)
        assertNotNull(headerCountdown)
        assertTrue(headerCountdown!!.startsWith("🐑 متبقي"))
        assertTrue(headerCountdown.contains("عيد الأضحى"))
    }

    @Test
    fun testApproachingOccasionsDuringRamadan() {
        val ramadanDate = HijriDate(
            day = 10,
            month = 9,
            year = 1448,
            monthName = "رمضان",
            dayOfWeekName = "الأحد",
            gregorianDay = 18,
            gregorianMonth = 2,
            gregorianYear = 2027
        )
        val occasion = HijriCalendarHelper.getNearestApproachingOccasion(ramadanDate)
        assertNotNull(occasion)
        assertTrue(occasion!!.isOngoingNow)
        assertTrue(occasion.formattedCountdown.contains("نحن الآن في شهر رمضان"))
    }

    @Test
    fun testQiblaBearingCalculation() {
        val cairoBearing = QiblaCalculator.calculateQiblaBearing(30.0444, 31.2357)
        assertEquals(136, cairoBearing.roundToInt())

        val medinaBearing = QiblaCalculator.calculateQiblaBearing(24.4672, 39.6111)
        assertTrue(medinaBearing in 170.0..180.0)

        val medinaDist = QiblaCalculator.calculateDistanceToKaabaKm(24.4672, 39.6111)
        assertTrue(medinaDist in 300.0..400.0)
    }

    @Test
    fun testQiblaAlignmentLogic() {
        val bearing = 136.0
        assertTrue(QiblaCalculator.isAligned(137f, bearing, 3.5f))
        assertFalse(QiblaCalculator.isAligned(180f, bearing, 3.5f))
    }
}
