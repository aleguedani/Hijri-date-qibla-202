package com.example.model

/**
 * Predefined major Islamic and international cities with accurate geographical coordinates.
 */
data class CityLocation(
    val id: String,
    val nameAr: String,
    val countryAr: String,
    val latitude: Double,
    val longitude: Double
) {
    companion object {
        val PRESET_CITIES = listOf(
            CityLocation("mecca", "مكة المكرمة", "المملكة العربية السعودية", 21.4225, 39.8262),
            CityLocation("medina", "المدينة المنورة", "المملكة العربية السعودية", 24.4672, 39.6111),
            CityLocation("riyadh", "الرياض", "المملكة العربية السعودية", 24.7136, 46.6753),
            CityLocation("jeddah", "جدة", "المملكة العربية السعودية", 21.5433, 39.1728),
            CityLocation("jerusalem", "القدس الشريف", "فلسطين", 31.7683, 35.2137),
            CityLocation("cairo", "القاهرة", "مصر", 30.0444, 31.2357),
            CityLocation("alexandria", "الإسكندرية", "مصر", 31.2001, 29.9187),
            CityLocation("amman", "عمان", "الأردن", 31.9539, 35.9106),
            CityLocation("damascus", "دمشق", "سوريا", 33.5138, 36.2765),
            CityLocation("beirut", "بيروت", "لبنان", 33.8938, 35.5018),
            CityLocation("baghdad", "بغداد", "العراق", 33.3152, 44.3661),
            CityLocation("kuwait", "الكويت", "الكويت", 29.3759, 47.9774),
            CityLocation("doha", "الدوحة", "قطر", 25.2854, 51.5310),
            CityLocation("manama", "المنامة", "البحرين", 26.2285, 50.5860),
            CityLocation("abu_dhabi", "أبوظبي", "الإمارات العربية المتحدة", 24.4539, 54.3773),
            CityLocation("dubai", "دبي", "الإمارات العربية المتحدة", 25.2048, 55.2708),
            CityLocation("muscat", "مسقط", "عُمان", 23.5880, 58.3829),
            CityLocation("sanaa", "صنعاء", "اليمن", 15.3694, 44.1910),
            CityLocation("tripoli", "طرابلس", "ليبيا", 32.8872, 13.1913),
            CityLocation("tunis", "تونس", "تونس", 36.8065, 10.1815),
            CityLocation("algiers", "الجزائر", "الجزائر", 36.7538, 3.0588),
            CityLocation("rabat", "الرباط", "المغرب", 34.0209, -6.8416),
            CityLocation("casablanca", "الدار البيضاء", "المغرب", 33.5731, -7.5898),
            CityLocation("khartoum", "الخرطوم", "السودان", 15.5007, 32.5599),
            CityLocation("istanbul", "إسطنبول", "تركيا", 41.0082, 28.9784),
            CityLocation("jakarta", "جاكرتا", "إندونيسيا", -6.2088, 106.8456),
            CityLocation("kuala_lumpur", "كوالالمبور", "ماليزيا", 3.1390, 101.6869),
            CityLocation("islamabad", "إسلام آباد", "باكستان", 33.6844, 73.0479),
            CityLocation("london", "لندن", "المملكة المتحدة", 51.5074, -0.1278),
            CityLocation("paris", "باريس", "فرنسا", 48.8566, 2.3522),
            CityLocation("berlin", "برلين", "ألمانيا", 52.5200, 13.4050),
            CityLocation("new_york", "نيويورك", "الولايات المتحدة", 40.7128, -74.0060),
            CityLocation("toronto", "تورونتو", "كندا", 43.6532, -79.3832)
        )

        val DEFAULT_CITY = PRESET_CITIES[5] // Cairo as widely accessible default
    }
}
