package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.localization.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "jio_panal_prefs")

data class UserPrefsState(
    val currentUserId: String? = null,
    val isDarkMode: Boolean = true,
    val biometricEnabled: Boolean = false,
    val securityPin: String = "9076",
    val language: AppLanguage = AppLanguage.ENGLISH
)

class UserPreferences(private val context: Context) {

    private object Keys {
        val CURRENT_USER_ID = stringPreferencesKey("current_user_id")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val SECURITY_PIN = stringPreferencesKey("security_pin")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val prefsFlow: Flow<UserPrefsState> = context.dataStore.data.map { prefs ->
        val langCode = prefs[Keys.LANGUAGE] ?: AppLanguage.ENGLISH.code
        val lang = AppLanguage.entries.firstOrNull { it.code == langCode } ?: AppLanguage.ENGLISH
        UserPrefsState(
            currentUserId = prefs[Keys.CURRENT_USER_ID],
            isDarkMode = prefs[Keys.IS_DARK_MODE] ?: true,
            biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
            securityPin = prefs[Keys.SECURITY_PIN] ?: "9076",
            language = lang
        )
    }

    suspend fun setCurrentUserId(userId: String?) {
        context.dataStore.edit { prefs ->
            if (userId == null) {
                prefs.remove(Keys.CURRENT_USER_ID)
            } else {
                prefs[Keys.CURRENT_USER_ID] = userId
            }
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_DARK_MODE] = enabled }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setSecurityPin(pin: String) {
        context.dataStore.edit { prefs -> prefs[Keys.SECURITY_PIN] = pin }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs -> prefs[Keys.LANGUAGE] = language.code }
    }
}
