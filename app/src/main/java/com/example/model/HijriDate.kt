package com.example.model

/**
 * Represents a complete Hijri date with localized Arabic names and Islamic occasions.
 */
data class HijriDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val monthName: String,
    val dayOfWeekName: String,
    val gregorianDay: Int,
    val gregorianMonth: Int,
    val gregorianYear: Int,
    val adjustmentDays: Int = 0,
    val islamicEvent: String? = null
) {
    /**
     * E.g. «الثلاثاء، 8 ربيع الآخر 1448 هـ»
     */
    fun getFullFormattedHijri(): String {
        return "$dayOfWeekName، $day $monthName $year هـ"
    }

    /**
     * E.g. «8 ربيع الآخر 1448 هـ»
     */
    fun getShortFormattedHijri(): String {
        return "$day $monthName $year هـ"
    }

    /**
     * E.g. «20 أكتوبر 2026 م»
     */
    fun getFormattedGregorian(): String {
        val gregorianMonths = listOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        )
        val monthStr = if (gregorianMonth in 1..12) gregorianMonths[gregorianMonth - 1] else "$gregorianMonth"
        return "$gregorianDay $monthStr $gregorianYear م"
    }
}
