package com.daysleft.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import com.daysleft.R
import com.daysleft.data.DayStatus
import com.daysleft.data.PreferencesManager
import com.daysleft.databinding.OverlayDaysLeftBinding
import com.daysleft.util.Constants
import com.daysleft.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manages the overlay window lifecycle and display.
 * Shows a day-counter overlay with a dot-grid; Good/Bad day evaluation is no longer present.
 */
class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val prefsManager = PreferencesManager(context)

    private var overlayView: android.view.View? = null
    private var binding: OverlayDaysLeftBinding? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var autoDismissJob: Job? = null

    /**
     * Show the overlay
     */
    fun showOverlay() {
        if (overlayView != null) return

        scope.launch {
            try {
                val themedContext = android.view.ContextThemeWrapper(context, R.style.Theme_DaysLeft)
                val inflater = LayoutInflater.from(themedContext)
                binding = OverlayDaysLeftBinding.inflate(inflater)
                overlayView = binding?.root

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                    PixelFormat.TRANSLUCENT
                )
                params.gravity = Gravity.CENTER

                setupOverlayUI()
                windowManager.addView(overlayView, params)
                fadeIn()
                handleAutoDismiss()

            } catch (e: Exception) {
                android.util.Log.e("OverlayManager", "Error showing overlay", e)
                cleanup()
            }
        }
    }

    private suspend fun setupOverlayUI() = withContext(Dispatchers.Main) {
        binding?.apply {
            val currentDay = DateUtils.getCurrentDayOfYear()
            val totalDays = DateUtils.getTotalDaysInCurrentYear()

            // Set counter text
            tvOverlayCounter.text = context.getString(R.string.days_counter, currentDay, totalDays)

            // Build day statuses from pure date arithmetic — no DB needed
            val dayStatuses = buildDayStatuses(currentDay, totalDays)
            overlayDotGrid.setDayStatuses(dayStatuses)
            overlayDotGrid.updateYearData(currentDay, totalDays)

            // Setup dismiss button based on user preference
            val dismissMode = prefsManager.dismissMode.first()
            if (dismissMode == Constants.DismissMode.MANUAL) {
                btnDismiss.visibility = android.view.View.VISIBLE
                btnDismiss.setOnClickListener { dismissOverlay() }
            } else {
                btnDismiss.visibility = android.view.View.GONE
            }
        }
    }

    private fun buildDayStatuses(currentDay: Int, totalDays: Int): Map<Int, DayStatus> {
        val statusMap = mutableMapOf<Int, DayStatus>()
        for (day in 1..totalDays) {
            statusMap[day] = when {
                day < currentDay  -> DayStatus.PAST
                day == currentDay -> DayStatus.CURRENT
                else              -> DayStatus.FUTURE
            }
        }
        return statusMap
    }

    private suspend fun handleAutoDismiss() {
        val dismissMode = prefsManager.dismissMode.first()
        if (dismissMode == Constants.DismissMode.AUTO) {
            val duration = prefsManager.autoDismissDuration.first()
            autoDismissJob = scope.launch {
                delay(duration * 1000L)
                dismissOverlay()
            }
        }
    }

    private fun fadeIn() {
        overlayView?.alpha = 0f
        overlayView?.animate()
            ?.alpha(1f)
            ?.setDuration(Constants.ANIMATION_DURATION_FADE)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()
    }

    /**
     * Dismiss the overlay with fade out animation
     */
    fun dismissOverlay() {
        overlayView?.animate()
            ?.alpha(0f)
            ?.setDuration(Constants.ANIMATION_DURATION_FADE)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.withEndAction { cleanup() }
            ?.start()
    }

    private fun cleanup() {
        try {
            autoDismissJob?.cancel()
            overlayView?.let { windowManager.removeView(it) }
            overlayView = null
            binding = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Check if overlay is currently showing
     */
    fun isShowing(): Boolean = overlayView != null
}
