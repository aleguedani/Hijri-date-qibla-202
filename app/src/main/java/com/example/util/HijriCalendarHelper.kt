package com.example.util

import com.example.model.ApproachingOccasion
import com.example.model.HijriDate
import java.util.Calendar

/**
 * Utility for astronomical and tabular conversion between Gregorian and Hijri calendars,
 * including Islamic occasions, user lunar sighting adjustments (±1, ±2 days),
 * and countdown calculations for the 3 designated near occasions (Ramadan, Eid al-Adha, Muharram).
 */
object HijriCalendarHelper {

    val HIJRI_MONTH_NAMES_AR = listOf(
        "محرم",
        "صفر",
        "ربيع الأول",
        "ربيع الآخر",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب",
        "شعبان",
        "رمضان",
        "شوال",
        "ذو القعدة",
        "ذو الحجة"
    )

    val WEEKDAY_NAMES_AR = listOf(
        "الأحد",
        "الإثنين",
        "الثلاثاء",
        "الأربعاء",
        "الخميس",
        "الجمعة",
        "السبت"
    )

    /**
     * Converts a Gregorian date (year, month 1..12, day 1..31) to HijriDate.
     */
    fun convertGregorianToHijri(
        year: Int,
        month: Int,
        day: Int,
        adjustmentDays: Int = 0
    ): HijriDate {
        val jdn = gregorianToJdn(year, month, day) + adjustmentDays
        val (hYear, hMonth, hDay) = jdnToHijri(jdn)

        val dayOfWeekIndex = (((jdn + 1) % 7 + 7) % 7).toInt()
        val dayOfWeekName = WEEKDAY_NAMES_AR.getOrElse(dayOfWeekIndex) { "اليوم" }

        val safeMonth = hMonth.coerceIn(1, 12)
        val monthName = HIJRI_MONTH_NAMES_AR[safeMonth - 1]
        val event = getIslamicEvent(safeMonth, hDay)

        return HijriDate(
            day = hDay,
            month = safeMonth,
            year = hYear,
            monthName = monthName,
            dayOfWeekName = dayOfWeekName,
            gregorianDay = day,
            gregorianMonth = month,
            gregorianYear = year,
            adjustmentDays = adjustmentDays,
            islamicEvent = event
        )
    }

    /**
     * Gets today's Hijri date based on system calendar and user adjustment.
     */
    fun getTodayHijri(adjustmentDays: Int = 0): HijriDate {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return convertGregorianToHijri(year, month, day, adjustmentDays)
    }

    /**
     * Calculates Julian Day Number for a given Gregorian date.
     */
    fun gregorianToJdn(year: Int, month: Int, day: Int): Long {
        var y = year.toLong()
        var m = month.toLong()
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + (a / 4)
        return (365.25 * (y + 4716)).toLong() + (30.6001 * (m + 1)).toLong() + day.toLong() + b - 1524
    }

    /**
     * Converts a tabular Hijri date to Julian Day Number (JDN).
     */
    fun hijriToJdn(year: Int, month: Int, day: Int): Long {
        val epochJdn = 1948440L
        val y = (year - 1).coerceAtLeast(0)
        val cycleNumber = y / 30
        val yearInCycle = y % 30

        val leapPattern = intArrayOf(
            354, 355, 354, 354, 355, 354, 355, 354, 354, 355,
            354, 354, 355, 354, 354, 355, 354, 355, 354, 354,
            355, 354, 354, 355, 354, 355, 354, 354, 355, 354
        )

        var days = cycleNumber.toLong() * 10631L
        for (i in 0 until yearInCycle) {
            days += leapPattern[i]
        }

        val isLeap = leapPattern[yearInCycle] == 355
        for (m in 1 until month) {
            val mLength = if (m == 12) {
                if (isLeap) 30 else 29
            } else if (m % 2 == 1) {
                30
            } else {
                29
            }
            days += mLength
        }

        days += (day - 1)
        return epochJdn + days
    }

    /**
     * Converts a Julian Day Number (JDN) to Hijri (year, month 1..12, day 1..30)
     * using the standard astronomical Kuwaiti tabular algorithm.
     */
    private fun jdnToHijri(jdn: Long): Triple<Int, Int, Int> {
        val epochJdn = 1948440L // Civil Islamic epoch
        val daysSinceEpoch = jdn - epochJdn + 1

        val cycleNumber = (daysSinceEpoch - 1) / 10631
        var remainingDays = daysSinceEpoch - 1 - (cycleNumber * 10631)

        val leapPattern = intArrayOf(
            354, 355, 354, 354, 355, 354, 355, 354, 354, 355,
            354, 354, 355, 354, 354, 355, 354, 355, 354, 354,
            355, 354, 354, 355, 354, 355, 354, 354, 355, 354
        )

        var yearInCycle = 0
        for (i in 0 until 30) {
            val yLen = leapPattern[i]
            if (remainingDays < yLen) {
                yearInCycle = i + 1
                break
            }
            remainingDays -= yLen
        }

        if (yearInCycle == 0) {
            yearInCycle = 30
        }

        val hijriYear = (cycleNumber * 30 + yearInCycle).toInt()

        val isLeap = leapPattern[yearInCycle - 1] == 355
        var month = 1
        var day = (remainingDays + 1).toInt()

        for (m in 1..12) {
            val mLength = if (m == 12) {
                if (isLeap) 30 else 29
            } else if (m % 2 == 1) {
                30
            } else {
                29
            }

            if (day <= mLength) {
                month = m
                break
            }
            day -= mLength
        }

        return Triple(hijriYear, month, day.coerceAtLeast(1))
    }

    /**
     * Calculates the remaining time to the nearest occasion among ONLY the 3 designated events:
     * 1. شهر رمضان (1 رمضان)
     * 2. عيد الأضحى (10 ذو الحجة)
     * 3. شهر محرم (1 محرم)
     *
     * Returns null if the nearest occasion is far away (more than [proximityThresholdDays], default 90 days ~ 3 months).
     */
    fun getNearestApproachingOccasion(
        hijriDate: HijriDate,
        proximityThresholdDays: Int = 90
    ): ApproachingOccasion? {
        val y = hijriDate.year
        val m = hijriDate.month
        val d = hijriDate.day

        // 1. Ongoing checks if currently in the occasion
        if (m == 9) {
            return ApproachingOccasion(
                nameAr = "شهر رمضان",
                remainingDays = 0,
                formattedCountdown = "نحن الآن في شهر رمضان المبارك (تقبل الله طاعاتكم)",
                isOngoingNow = true
            )
        }
        if (m == 12 && d in 10..13) {
            return ApproachingOccasion(
                nameAr = "عيد الأضحى",
                remainingDays = 0,
                formattedCountdown = "نحن الآن في أيام عيد الأضحى المبارك (عيدكم مبارك)",
                isOngoingNow = true
            )
        }
        if (m == 1 && d == 1) {
            return ApproachingOccasion(
                nameAr = "شهر محرم",
                remainingDays = 0,
                formattedCountdown = "اليوم هو رأس السنة الهجرية (1 محرم)",
                isOngoingNow = true
            )
        }

        val currentJdn = hijriToJdn(y, m, d)

        // Target 1: Ramadan (Month 9, Day 1)
        val ramadanTargetYear = if (m < 9 || (m == 9 && d == 1)) y else y + 1
        val ramadanJdn = hijriToJdn(ramadanTargetYear, 9, 1)
        val ramadanDays = (ramadanJdn - currentJdn).toInt()

        // Target 2: Eid al-Adha (Month 12, Day 10)
        val eidTargetYear = if (m < 12 || (m == 12 && d <= 10)) y else y + 1
        val eidJdn = hijriToJdn(eidTargetYear, 12, 10)
        val eidDays = (eidJdn - currentJdn).toInt()

        // Target 3: Muharram (Month 1, Day 1)
        val muharramTargetYear = y + 1 // since m > 1 or (m == 1 && d > 1)
        val muharramJdn = hijriToJdn(muharramTargetYear, 1, 1)
        val muharramDays = (muharramJdn - currentJdn).toInt()

        // Candidate list with positive remaining days
        val candidates = listOf(
            Triple("شهر رمضان", ramadanDays, "شهر رمضان"),
            Triple("عيد الأضحى", eidDays, "عيد الأضحى المبارك"),
            Triple("شهر محرم", muharramDays, "شهر محرم (رأس السنة)")
        ).filter { it.second > 0 }

        val nearest = candidates.minByOrNull { it.second } ?: return null

        // If the nearest occasion is far away (exceeds threshold, e.g. > 90 days / ~3 months), do not display
        if (nearest.second > proximityThresholdDays) {
            return null
        }

        val countdownText = formatCountdownString(nearest.second, nearest.first)
        return ApproachingOccasion(
            nameAr = nearest.first,
            remainingDays = nearest.second,
            formattedCountdown = countdownText,
            isOngoingNow = false
        )
    }

    /**
     * Formats remaining days into precise, natural Arabic phrasing (days and months).
     */
    private fun formatCountdownString(days: Int, occasionName: String): String {
        return when {
            days == 1 -> "متبقٍ يوم واحد على $occasionName"
            days == 2 -> "متبقٍ يومان على $occasionName"
            days in 3..10 -> "متبقٍ $days أيام على $occasionName"
            days in 11..29 -> "متبقٍ $days يوماً على $occasionName"
            days in 30..59 -> {
                val remDays = days - 30
                when (remDays) {
                    0 -> "متبقٍ شهر واحد على $occasionName"
                    1 -> "متبقٍ شهر ويوم واحد على $occasionName"
                    2 -> "متبقٍ شهر ويومان على $occasionName"
                    in 3..10 -> "متبقٍ شهر و$remDays أيام على $occasionName"
                    else -> "متبقٍ شهر و$remDays يوماً على $occasionName"
                }
            }
            else -> {
                val remDays = days - 60
                when (remDays) {
                    0 -> "متبقٍ شهران على $occasionName"
                    1 -> "متبقٍ شهران ويوم واحد على $occasionName"
                    2 -> "متبقٍ شهران ويومان على $occasionName"
                    in 3..10 -> "متبقٍ شهران و$remDays أيام على $occasionName"
                    else -> "متبقٍ شهران و$remDays يوماً على $occasionName"
                }
            }
        }
    }

    /**
     * Checks if a given Hijri date falls on a significant Islamic occasion.
     */
    fun getIslamicEvent(month: Int, day: Int): String? {
        return when {
            month == 1 && day == 1 -> "رأس السنة الهجرية الجديدة"
            month == 1 && day == 9 -> "تاسوعاء"
            month == 1 && day == 10 -> "يوم عاشوراء المبارك"
            month == 3 && day == 12 -> "المولد النبوي الشريف"
            month == 7 && day == 27 -> "ذكرى الإسراء والمعراج"
            month == 8 && day == 15 -> "ليلة النصف من شعبان"
            month == 9 && day == 1 -> "أول أيام شهر رمضان المبارك"
            month == 9 && day in 21..30 && day % 2 == 1 -> if (day == 27) "ليلة القدر المباركة (أرجى الليالي)" else "من الليالي الوترية في رمضان"
            month == 10 && day == 1 -> "عيد الفطر المبارك"
            month == 10 && day in 2..3 -> "أيام عيد الفطر السعيد"
            month == 12 && day == 1 -> "أول ذي الحجة (عشر ذي الحجة المباركة)"
            month == 12 && day in 2..8 -> "عشر ذي الحجة المباركة"
            month == 12 && day == 8 -> "يوم التروية"
            month == 12 && day == 9 -> "يوم عرفة المبارك (خير يوم طلعت عليه الشمس)"
            month == 12 && day == 10 -> "عيد الأضحى المبارك (يوم النحر)"
            month == 12 && day in 11..13 -> "أيام التشريق المباركة"
            else -> null
        }
    }

    /**
     * Calculates countdown for the header (replacing Bismillah) according to exact user conditions:
     * 1. Before Ramadan: If upcoming Ramadan is less than 3 months away (< 90 days), returns
     *    countdown e.g. «🌙 متبقي 45 يومًا على رمضان». If 3 months or more (>= 90 days), returns null (nothing displayed).
     * 2. After Ramadan ends (Months 10, 11, and 12 up to Day 10): returns countdown to Eid al-Adha
     *    e.g. «🐑 متبقي 30 يومًا على عيد الأضحى».
     * 3. During Ramadan itself: returns «🌙 شهر رمضان المبارك».
     * 4. Only one occasion at any time, and no other occasions.
     */
    fun getHeaderCountdown(hijriDate: HijriDate): String? {
        val y = hijriDate.year
        val m = hijriDate.month
        val d = hijriDate.day
        val currentJdn = hijriToJdn(y, m, d)

        // Case 1: After Ramadan ends (Month 10, 11, 12 up to Day 10) -> Eid al-Adha countdown
        if (m in 10..12) {
            val eidJdn = hijriToJdn(y, 12, 10)
            val daysToEid = (eidJdn - currentJdn).toInt()
            if (daysToEid > 0) {
                return formatHeaderCountdown(daysToEid, "عيد الأضحى", "🐑")
            } else if (daysToEid == 0) {
                return "🐑 اليوم هو عيد الأضحى المبارك"
            } else if (m == 12 && d in 11..13) {
                return "🐑 تقبل الله طاعاتكم (عيد الأضحى)"
            }
            // After Eid al-Adha in Month 12: Next Ramadan is ~250 days away (>= 90 days) -> returns null
            return null
        }

        // Case 2: During Ramadan (Month 9)
        if (m == 9) {
            return if (d == 1) "🌙 اليوم أول أيام شهر رمضان المبارك" else "🌙 شهر رمضان المبارك"
        }

        // Case 3: Before Ramadan (Months 1..8)
        val ramadanJdn = hijriToJdn(y, 9, 1)
        val daysToRamadan = (ramadanJdn - currentJdn).toInt()

        // "إذا كان موعد رمضان القادم يبعد أقل من 3 أشهر، اعرض المدة المتبقية عليه. إذا كان يبعد 3 أشهر أو أكثر، فلا تعرض شيئًا"
        if (daysToRamadan in 1..89) { // Strictly less than 3 months (< 90 days)
            return formatHeaderCountdown(daysToRamadan, "رمضان", "🌙")
        }

        return null
    }

    private fun formatHeaderCountdown(days: Int, occasionName: String, emoji: String): String {
        val daysText = when (days) {
            1 -> "يوم واحد"
            2 -> "يومان"
            in 3..10 -> "$days أيام"
            else -> "$days يومًا"
        }
        return "$emoji متبقي $daysText على $occasionName"
    }
}
