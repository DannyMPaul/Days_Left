package com.daysleft.util

object Constants {
    // Grid dimensions (52 weeks × 7 days = 364 + overflow handled by day count)
    const val GRID_COLUMNS = 7
    const val GRID_ROWS = 52

    // Year lengths
    const val DAYS_IN_YEAR = 365
    const val DAYS_IN_LEAP_YEAR = 366

    // Overlay fade animation
    const val ANIMATION_DURATION_FADE = 300L

    // Hourly trigger interval
    const val HOURLY_TRIGGER_INTERVAL_MS = 3_600_000L // 1 hour

    // Default auto-dismiss duration
    const val DEFAULT_AUTO_DISMISS_DURATION = 3 // seconds

    // DataStore preference keys
    object Prefs {
        const val TRIGGER_MODE = "trigger_mode"
        const val DISMISS_MODE = "dismiss_mode"
        const val AUTO_DISMISS_DURATION = "auto_dismiss_duration"
        const val INCLUDED_APPS = "included_apps"
        const val LAST_SHOWN_TIMESTAMP = "last_shown_timestamp"
        const val LAST_UNLOCK_DATE = "last_unlock_date"
    }

    enum class TriggerMode {
        EVERY_APP,
        HOURLY,
        FIRST_APP_OF_DAY
    }

    enum class DismissMode {
        AUTO,
        MANUAL
    }
}
