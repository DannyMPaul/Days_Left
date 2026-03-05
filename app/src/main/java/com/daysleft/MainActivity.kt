package com.daysleft

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.daysleft.BuildConfig
import com.daysleft.data.DayStatus
import com.daysleft.data.PreferencesManager
import com.daysleft.databinding.ActivityMainBinding
import com.daysleft.util.DateUtils
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            prefsManager = PreferencesManager(applicationContext)
            setupPermissionButtons()
            setupUI()
            checkPermissions()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private fun checkPermissions() {
        try {
            val hasOverlayPermission = Settings.canDrawOverlays(this)
            val hasAccessibilityPermission = isAccessibilityServiceEnabled()

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Overlay=$hasOverlayPermission  Accessibility=$hasAccessibilityPermission")
            }

            if (hasOverlayPermission && hasAccessibilityPermission) {
                binding.permissionsLayout.visibility = View.GONE
                binding.mainContent.visibility = View.VISIBLE
                loadYearData()
            } else {
                binding.permissionsLayout.visibility = View.VISIBLE
                binding.mainContent.visibility = View.GONE
                updatePermissionStatus(hasOverlayPermission, hasAccessibilityPermission)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkPermissions", e)
            Toast.makeText(this, "Permission check error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun forceCheckPermissions() {
        val hasOverlayPermission = Settings.canDrawOverlays(this)
        val hasAccessibilityPermission = isAccessibilityServiceEnabled()
        updatePermissionStatus(hasOverlayPermission, hasAccessibilityPermission)
        if (hasOverlayPermission && hasAccessibilityPermission) {
            Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show()
            binding.permissionsLayout.visibility = View.GONE
            binding.mainContent.visibility = View.VISIBLE
            loadYearData()
        }
    }

    private fun setupPermissionButtons() {
        binding.btnEnableOverlay.setOnClickListener {
            Toast.makeText(this, "Opening overlay settings…", Toast.LENGTH_SHORT).show()
            requestOverlayPermission()
        }
        binding.btnEnableAccessibility.setOnClickListener {
            Toast.makeText(this, "Opening accessibility settings…", Toast.LENGTH_SHORT).show()
            openAccessibilitySettings()
        }
        binding.btnCheckPermissions.setOnClickListener {
            Toast.makeText(this, "Checking permissions…", Toast.LENGTH_SHORT).show()
            forceCheckPermissions()
        }
    }

    private fun updatePermissionStatus(hasOverlay: Boolean, hasAccessibility: Boolean) {
        val status = when {
            hasOverlay && hasAccessibility -> "✓ All permissions granted!"
            hasOverlay    -> "✓ Overlay granted. Please enable Accessibility Service."
            hasAccessibility -> "✓ Accessibility granted. Please enable Display Over Apps."
            else -> "Please grant both permissions to continue."
        }
        binding.tvPermissionsStatus.text = status
        binding.tvPermissionsStatus.visibility = View.VISIBLE
        Toast.makeText(this, status, Toast.LENGTH_LONG).show()

        if (hasOverlay && hasAccessibility) {
            binding.tvPermissionsStatus.postDelayed({ checkPermissions() }, 1000)
        }
    }

    private fun requestOverlayPermission() {
        try {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                Toast.makeText(this, "Overlay permission already granted", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting overlay permission", e)
            Toast.makeText(this, "Error opening settings: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openAccessibilitySettings() {
        try {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } catch (e: Exception) {
            Log.e(TAG, "Error opening accessibility settings", e)
            Toast.makeText(this, "Error opening settings: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName = "${packageName}/com.daysleft.service.AppLaunchAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        return enabledServices.split(":").any { it.equals(serviceName, ignoreCase = true) }
    }

    private fun setupUI() {
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun loadYearData() {
        lifecycleScope.launch {
            try {
                val currentDay = DateUtils.getCurrentDayOfYear()
                val totalDays = DateUtils.getTotalDaysInCurrentYear()

                binding.tvDaysCounter.text = getString(R.string.days_counter, currentDay, totalDays)

                val dayStatuses = mutableMapOf<Int, DayStatus>()
                for (day in 1..totalDays) {
                    dayStatuses[day] = when {
                        day < currentDay  -> DayStatus.PAST
                        day == currentDay -> DayStatus.CURRENT
                        else              -> DayStatus.FUTURE
                    }
                }
                binding.dotGridView.setDayStatuses(dayStatuses)
                binding.dotGridView.updateYearData(currentDay, totalDays)

            } catch (e: Exception) {
                Log.e(TAG, "Error loading year data", e)
            }
        }
    }
}
