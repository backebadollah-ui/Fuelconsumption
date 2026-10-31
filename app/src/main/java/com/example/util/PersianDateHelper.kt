package com.example.util

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class PersianDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int    // 1..31
) {
    fun toFormattedString(includeMonthName: Boolean = false): String {
        return if (includeMonthName && month in 1..12) {
            val monthName = PersianDateHelper.PERSIAN_MONTH_NAMES[month - 1]
            "$day $monthName $year"
        } else {
            val m = month.toString().padStart(2, '0')
            val d = day.toString().padStart(2, '0')
            "$year/$m/$d"
        }
    }
}

object PersianDateHelper {

    val PERSIAN_MONTH_NAMES = listOf(
        "فروردین",
        "اردیبهشت",
        "خرداد",
        "تیر",
        "مرداد",
        "شهریور",
        "مهر",
        "آبان",
        "آذر",
        "دی",
        "بهمن",
        "اسفند"
    )

    val PERSIAN_MONTH_NAMES_EN = listOf(
        "Farvardin",
        "Ordibehesht",
        "Khordad",
        "Tir",
        "Mordad",
        "Shahrivar",
        "Mehr",
        "Aban",
        "Azar",
        "Dey",
        "Bahman",
        "Esfand"
    )

    fun isPersianLeapYear(jy: Int): Boolean {
        // A year is a leap year if the day after 29 Esfand in Gregorian falls on the same Jalali year (30 Esfand)
        val (_, gm, gd) = jalaliToGregorian(jy, 12, 29)
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.YEAR, gm)
            set(Calendar.MONTH, gm - 1)
            set(Calendar.DAY_OF_MONTH, gd)
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val nextJDate = gregorianToPersian(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        return nextJDate.month == 12 && nextJDate.day == 30
    }

    fun getDaysInPersianMonth(year: Int, month: Int): Int {
        return when {
            month in 1..6 -> 31
            month in 7..11 -> 30
            month == 12 -> if (isPersianLeapYear(year)) 30 else 29
            else -> 30
        }
    }

    /**
     * Converts Gregorian date (year, month 1..12, day 1..31) to Persian Solar date (year, month 1..12, day 1..31)
     */
    fun gregorianToPersian(gy: Int, gm: Int, gd: Int): PersianDate {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gDaysInMonth[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return PersianDate(jy, jm, jd)
    }

    /**
     * Converts Persian Solar date (year, month 1..12, day 1..31) to Gregorian date (gy, gm, gd)
     */
    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jyAdj = jy + 1595
        var days = -355668 + (365 * jyAdj) + ((jyAdj / 33) * 8) + (((jyAdj % 33) + 3) / 4) + jd
        days += if (jm < 7) {
            (jm - 1) * 31
        } else {
            ((jm - 7) * 30) + 186
        }
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            days -= 1
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) {
                days += 1
            }
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd = days + 1
        val isGLeap = (gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)
        val salA = if (isGLeap) {
            intArrayOf(0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        } else {
            intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        }
        var gm = 0
        while (gm < 13 && gd > salA[gm]) {
            gd -= salA[gm]
            gm += 1
        }
        return Triple(gy, gm, gd)
    }

    /**
     * Converts epoch milliseconds to Persian Date
     */
    fun millisToPersian(millis: Long): PersianDate {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return gregorianToPersian(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Converts Persian Date to epoch milliseconds
     */
    fun persianToMillis(jy: Int, jm: Int, jd: Int, hour: Int = 12, minute: Int = 0): Long {
        val (gy, gm, gd) = jalaliToGregorian(jy, jm, jd)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, gy)
            set(Calendar.MONTH, gm - 1)
            set(Calendar.DAY_OF_MONTH, gd)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Formats a timestamp into a user-friendly date string based on language preference.
     * When languageIsFarsi is true, returns Solar Hijri (شمسی) date.
     */
    fun formatDisplayDate(millis: Long, languageIsFarsi: Boolean, includeMonthName: Boolean = true): String {
        return if (languageIsFarsi) {
            val pDate = millisToPersian(millis)
            pDate.toFormattedString(includeMonthName)
        } else {
            val cal = Calendar.getInstance().apply { timeInMillis = millis }
            val y = cal.get(Calendar.YEAR)
            val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
            val d = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
            "$y/$m/$d"
        }
    }

    /**
     * Converts English digits 0-9 in a string to Persian digits ۰-۹
     */
    fun toPersianDigits(text: String): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val builder = StringBuilder(text.length)
        for (char in text) {
            if (char in '0'..'9') {
                builder.append(persianDigits[char - '0'])
            } else {
                builder.append(char)
            }
        }
        return builder.toString()
    }
}
