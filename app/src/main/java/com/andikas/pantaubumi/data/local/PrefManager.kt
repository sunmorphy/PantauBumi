package com.andikas.pantaubumi.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val DARK_MODE = "dark_mode"
        private const val ONBOARDING_COMPLETED = "onboarding_completed"
        private const val DEVICE_ID = "device_id"
        private const val FCM_TOKEN = "fcm_token"

        private const val NOTIFY_FLOOD = "notify_flood"
        private const val NOTIFY_LANDSLIDE = "notify_landslide"
        private const val NOTIFY_EARTHQUAKE = "notify_earthquake"
        private const val MIN_RISK_THRESHOLD = "min_risk_threshold"
    }

    val isOnboardingCompleted: Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == ONBOARDING_COMPLETED) {
                trySend(prefs.getBoolean(ONBOARDING_COMPLETED, false))
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.onStart {
        emit(sharedPreferences.getBoolean(ONBOARDING_COMPLETED, false))
    }

    val isDarkMode: Flow<Boolean?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == DARK_MODE) {
                trySend(if (prefs.contains(DARK_MODE)) prefs.getBoolean(DARK_MODE, false) else null)
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.onStart {
        emit(
            if (sharedPreferences.contains(DARK_MODE)) sharedPreferences.getBoolean(
                DARK_MODE,
                false
            ) else null
        )
    }

    fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit {
            putBoolean(ONBOARDING_COMPLETED, completed)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(DARK_MODE, enabled)
        }
    }

    val deviceId: String
        get() {
            var id = sharedPreferences.getString(DEVICE_ID, null)
            if (id == null) {
                id = java.util.UUID.randomUUID().toString()
                sharedPreferences.edit { putString(DEVICE_ID, id) }
            }
            return id
        }

    fun setFcmToken(token: String?) {
        sharedPreferences.edit {
            putString(FCM_TOKEN, token)
        }
    }

    val fcmToken: String?
        get() = sharedPreferences.getString(FCM_TOKEN, null)

    val notifyFlood: Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == NOTIFY_FLOOD) trySend(prefs.getBoolean(NOTIFY_FLOOD, true))
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.onStart { emit(sharedPreferences.getBoolean(NOTIFY_FLOOD, true)) }

    val notifyLandslide: Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == NOTIFY_LANDSLIDE) trySend(prefs.getBoolean(NOTIFY_LANDSLIDE, true))
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.onStart { emit(sharedPreferences.getBoolean(NOTIFY_LANDSLIDE, true)) }

    val notifyEarthquake: Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == NOTIFY_EARTHQUAKE) trySend(prefs.getBoolean(NOTIFY_EARTHQUAKE, true))
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.onStart { emit(sharedPreferences.getBoolean(NOTIFY_EARTHQUAKE, true)) }

    val minRiskThreshold: Flow<Int> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == MIN_RISK_THRESHOLD) trySend(
                prefs.getInt(
                    MIN_RISK_THRESHOLD,
                    1
                )
            ) // 1 = Medium
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.onStart { emit(sharedPreferences.getInt(MIN_RISK_THRESHOLD, 1)) }

    fun setNotifyFlood(enabled: Boolean) =
        sharedPreferences.edit { putBoolean(NOTIFY_FLOOD, enabled) }

    fun setNotifyLandslide(enabled: Boolean) =
        sharedPreferences.edit { putBoolean(NOTIFY_LANDSLIDE, enabled) }

    fun setNotifyEarthquake(enabled: Boolean) =
        sharedPreferences.edit { putBoolean(NOTIFY_EARTHQUAKE, enabled) }

    fun setMinRiskThreshold(threshold: Int) =
        sharedPreferences.edit { putInt(MIN_RISK_THRESHOLD, threshold) }
}
