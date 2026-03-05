package com.daysleft.service

import android.accessibilityservice.AccessibilityService
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import com.daysleft.BuildConfig
import com.daysleft.data.PreferencesManager
import com.daysleft.overlay.OverlayManager
import com.daysleft.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Accessibility service that detects app launches and triggers the Days Left overlay.
 */
class AppLaunchAccessibilityService : AccessibilityService() {

    private lateinit var prefsManager: PreferencesManager
    private lateinit var overlayManager: OverlayManager
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // Debounce: suppress rapid re-triggers for the same package
    private var lastLaunchedPackage: String? = null
    private var lastLaunchTime: Long = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            prefsManager = PreferencesManager(applicationContext)
            overlayManager = OverlayManager(this)
        } catch (e: Exception) {
            android.util.Log.e("AccessibilityService", "Init error", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        val className   = event.className?.toString()   ?: return

        // Only react to actual Activity launches, not system drawers/panels
        if (!className.endsWith("Activity")) return
        if (isSystemPackage(packageName)) return

        if (BuildConfig.DEBUG) {
            android.util.Log.d("AccessibilityService", "Launch: $packageName / $className")
        }

        val now = System.currentTimeMillis()
        if (packageName == lastLaunchedPackage && (now - lastLaunchTime) <= 3_000) return
        lastLaunchedPackage = packageName
        lastLaunchTime = now

        handleAppLaunch(packageName)
    }

    override fun onInterrupt() {}

    private fun isSystemPackage(pkg: String): Boolean =
        pkg == "com.android.systemui" ||
        pkg == "com.android.launcher3" ||
        pkg == "com.google.android.apps.nexuslauncher" ||
        pkg.startsWith("com.android.launcher") ||
        pkg == packageName

    private fun handleAppLaunch(packageName: String) {
        scope.launch {
            try {
                if (!Settings.canDrawOverlays(applicationContext)) return@launch

                val includedApps = prefsManager.includedApps.first()
                if (!includedApps.contains(packageName)) return@launch

                if (shouldShowOverlay()) {
                    overlayManager.showOverlay()
                    prefsManager.setLastShownTimestamp(System.currentTimeMillis())
                }
            } catch (e: Exception) {
                android.util.Log.e("AccessibilityService", "handleAppLaunch error", e)
            }
        }
    }

    private suspend fun shouldShowOverlay(): Boolean {
        val triggerMode = prefsManager.triggerMode.first()
        val lastShown   = prefsManager.getLastShownTimestamp()
        val now         = System.currentTimeMillis()

        if (BuildConfig.DEBUG) {
            android.util.Log.d("AccessibilityService", "mode=$triggerMode elapsed=${now - lastShown}ms")
        }

        return when (triggerMode) {
            Constants.TriggerMode.EVERY_APP -> true

            Constants.TriggerMode.HOURLY ->
                (now - lastShown) >= Constants.HOURLY_TRIGGER_INTERVAL_MS

            Constants.TriggerMode.FIRST_APP_OF_DAY -> {
                val lastUnlock   = prefsManager.getLastUnlockDate()
                val today        = LocalDate.now().toString()
                if (lastUnlock != today) {
                    prefsManager.setLastUnlockDate(today)
                    true
                } else {
                    false
                }
            }
        }
    }
}
