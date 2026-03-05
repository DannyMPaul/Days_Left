package com.daysleft.util

import java.time.LocalDate
import java.time.Year

object DateUtils {

    /**
     * Returns whether the current year is a leap year.
     */
    fun isCurrentYearLeap(): Boolean = Year.now().isLeap

    /**
     * Returns the current day of year (1–365 or 1–366).
     */
    fun getCurrentDayOfYear(): Int = LocalDate.now().dayOfYear

    /**
     * Returns total days in the current year (365 or 366).
     */
    fun getTotalDaysInCurrentYear(): Int =
        if (isCurrentYearLeap()) Constants.DAYS_IN_LEAP_YEAR else Constants.DAYS_IN_YEAR
}
