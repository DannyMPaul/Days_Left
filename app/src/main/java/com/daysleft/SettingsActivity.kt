package com.daysleft

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.daysleft.data.PreferencesManager
import com.daysleft.databinding.ActivitySettingsBinding
import com.daysleft.util.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefsManager: PreferencesManager
    private var isLoadingSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        prefsManager = PreferencesManager(applicationContext)

        setupListeners()
        loadSettings()
    }

    private fun loadSettings() {
        isLoadingSettings = true

        lifecycleScope.launch {
            val triggerMode = prefsManager.triggerMode.first()
            when (triggerMode) {
                Constants.TriggerMode.EVERY_APP -> binding.rbEveryApp.isChecked = true
                Constants.TriggerMode.HOURLY -> binding.rbHourly.isChecked = true
                Constants.TriggerMode.FIRST_APP_OF_DAY -> binding.rbFirstApp.isChecked = true
            }

            val dismissMode = prefsManager.dismissMode.first()
            when (dismissMode) {
                Constants.DismissMode.AUTO -> binding.rbAutoDismiss.isChecked = true
                Constants.DismissMode.MANUAL -> binding.rbManualDismiss.isChecked = true
            }
            updateDismissDurationVisibility(dismissMode)

            val duration = prefsManager.autoDismissDuration.first()
            binding.seekBarDuration.progress = duration - 1
            updateDurationText(duration)

            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            binding.tvAbout.text = getString(R.string.about_summary, versionName)

            isLoadingSettings = false
        }
    }

    private fun setupListeners() {
        binding.rgTriggerMode.setOnCheckedChangeListener { _, checkedId ->
            if (isLoadingSettings) return@setOnCheckedChangeListener
            val mode = when (checkedId) {
                R.id.rbEveryApp -> Constants.TriggerMode.EVERY_APP
                R.id.rbHourly -> Constants.TriggerMode.HOURLY
                R.id.rbFirstApp -> Constants.TriggerMode.FIRST_APP_OF_DAY
                else -> Constants.TriggerMode.FIRST_APP_OF_DAY
            }
            lifecycleScope.launch { prefsManager.setTriggerMode(mode) }
        }

        binding.rgDismissMode.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rbAutoDismiss -> Constants.DismissMode.AUTO
                R.id.rbManualDismiss -> Constants.DismissMode.MANUAL
                else -> Constants.DismissMode.AUTO
            }
            lifecycleScope.launch {
                prefsManager.setDismissMode(mode)
                updateDismissDurationVisibility(mode)
            }
        }

        binding.seekBarDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateDurationText(progress + 1)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val duration = (seekBar?.progress ?: 0) + 1
                lifecycleScope.launch { prefsManager.setAutoDismissDuration(duration) }
            }
        })

        binding.btnManageExcludedApps.setOnClickListener {
            showIncludedAppsDialog()
        }
    }

    private fun updateDismissDurationVisibility(mode: Constants.DismissMode) {
        binding.autoDismissDurationLayout.visibility =
            if (mode == Constants.DismissMode.AUTO) View.VISIBLE else View.GONE
    }

    private fun updateDurationText(duration: Int) {
        binding.tvDurationValue.text = getString(R.string.auto_dismiss_duration_summary, duration)
    }

    private fun showIncludedAppsDialog() {
        lifecycleScope.launch {
            val installedApps = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                getInstalledUserApps()
            }

            if (installedApps.isEmpty()) {
                Toast.makeText(this@SettingsActivity, "No apps found", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val includedApps = prefsManager.includedApps.first().toMutableSet()
            val appNames = installedApps.map { it.first }.toTypedArray()
            val appPackages = installedApps.map { it.second }
            val checkedItems = BooleanArray(appNames.size) { i -> includedApps.contains(appPackages[i]) }

            AlertDialog.Builder(this@SettingsActivity)
                .setTitle(getString(R.string.included_apps_title))
                .setMultiChoiceItems(appNames, checkedItems) { _, which, isChecked ->
                    val pkg = appPackages[which]
                    if (isChecked) includedApps.add(pkg) else includedApps.remove(pkg)
                }
                .setPositiveButton("OK") { _, _ ->
                    lifecycleScope.launch { prefsManager.setIncludedApps(includedApps) }
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }

    private fun getInstalledUserApps(): List<Pair<String, String>> {
        val pm = packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            .filter { it.activityInfo.packageName != packageName }
            .map { resolveInfo ->
                val label = resolveInfo.loadLabel(pm).toString()
                val pkg = resolveInfo.activityInfo.packageName
                label to pkg
            }
            .distinctBy { it.second }
            .sortedBy { it.first }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
