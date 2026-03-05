package com.daysleft.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.daysleft.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "days_left_prefs")

/**
 * Manages persistent app preferences using Jetpack DataStore.
 */
class PreferencesManager(private val context: Context) {

    private val dataStore = context.dataStore

    private object Keys {
        val TRIGGER_MODE       = stringPreferencesKey(Constants.Prefs.TRIGGER_MODE)
        val DISMISS_MODE       = stringPreferencesKey(Constants.Prefs.DISMISS_MODE)
        val AUTO_DISMISS_DURATION = intPreferencesKey(Constants.Prefs.AUTO_DISMISS_DURATION)
        val INCLUDED_APPS      = stringSetPreferencesKey(Constants.Prefs.INCLUDED_APPS)
        val LAST_SHOWN_TIMESTAMP = longPreferencesKey(Constants.Prefs.LAST_SHOWN_TIMESTAMP)
        val LAST_UNLOCK_DATE   = stringPreferencesKey(Constants.Prefs.LAST_UNLOCK_DATE)
    }

    // ── Trigger Mode ──────────────────────────────────────────────────────────

    val triggerMode: Flow<Constants.TriggerMode> = dataStore.data.map { prefs ->
        val value = prefs[Keys.TRIGGER_MODE] ?: Constants.TriggerMode.FIRST_APP_OF_DAY.name
        Constants.TriggerMode.valueOf(value)
    }

    suspend fun setTriggerMode(mode: Constants.TriggerMode) {
        dataStore.edit { it[Keys.TRIGGER_MODE] = mode.name }
    }

    // ── Dismiss Mode ──────────────────────────────────────────────────────────

    val dismissMode: Flow<Constants.DismissMode> = dataStore.data.map { prefs ->
        val value = prefs[Keys.DISMISS_MODE] ?: Constants.DismissMode.AUTO.name
        Constants.DismissMode.valueOf(value)
    }

    suspend fun setDismissMode(mode: Constants.DismissMode) {
        dataStore.edit { it[Keys.DISMISS_MODE] = mode.name }
    }

    // ── Auto-dismiss Duration ─────────────────────────────────────────────────

    val autoDismissDuration: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.AUTO_DISMISS_DURATION] ?: Constants.DEFAULT_AUTO_DISMISS_DURATION
    }

    suspend fun setAutoDismissDuration(seconds: Int) {
        dataStore.edit { it[Keys.AUTO_DISMISS_DURATION] = seconds }
    }

    // ── Included Apps (Whitelist) ─────────────────────────────────────────────

    val includedApps: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[Keys.INCLUDED_APPS] ?: emptySet()
    }

    suspend fun setIncludedApps(apps: Set<String>) {
        dataStore.edit { it[Keys.INCLUDED_APPS] = apps }
    }

    // ── Last Overlay Shown Timestamp ──────────────────────────────────────────

    suspend fun getLastShownTimestamp(): Long =
        dataStore.data.map { it[Keys.LAST_SHOWN_TIMESTAMP] ?: 0L }.first()

    suspend fun setLastShownTimestamp(timestamp: Long) {
        dataStore.edit { it[Keys.LAST_SHOWN_TIMESTAMP] = timestamp }
    }

    // ── Last Unlock Date (for FIRST_APP_OF_DAY trigger) ───────────────────────

    suspend fun getLastUnlockDate(): String? =
        dataStore.data.map { it[Keys.LAST_UNLOCK_DATE] }.first()

    suspend fun setLastUnlockDate(date: String) {
        dataStore.edit { it[Keys.LAST_UNLOCK_DATE] = date }
    }
}
