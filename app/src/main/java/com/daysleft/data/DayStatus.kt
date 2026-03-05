package com.daysleft.data

/**
 * Status of a single dot in the year grid.
 */
enum class DayStatus {
    FUTURE,   // Near-black teal — day hasn't happened yet
    CURRENT,  // Bright cyan — today
    PAST      // Dim teal — day has already passed
}
