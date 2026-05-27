package com.example.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.example.network.supabaseClient

data class SettingsState(
    val fontSize: Int = 1, // 0: Small, 1: Medium, 2: Large, 3: Extra Large
    val highContrast: Boolean = false,
    val largeButtonMode: Boolean = false,
    val reduceAnimation: Boolean = false,
    val masterNotifications: Boolean = true,
    val prayerNotifications: Boolean = true,
    val medicineReminders: Boolean = true,
    val hadithNotifications: Boolean = true,
    val newsNotifications: Boolean = true,
    val familyCheckIn: Boolean = true,
    val weatherAlerts: Boolean = true,
    val theme: Int = 0, // 0: System, 1: Light, 2: Dark
    val colorTheme: Int = 0, // 0: Green, 1: Blue, 2: Purple
    val bengaliNumerals: Boolean = true,
    val journalPinLock: Boolean = false,
    val language: Int = 0 // 0: Bengali, 1: English
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("ammu_settings", android.content.Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        // Load settings from SharedPreferences
        _state.value = SettingsState(
            fontSize = prefs.getInt("fontSize", 1),
            highContrast = prefs.getBoolean("highContrast", false),
            largeButtonMode = prefs.getBoolean("largeButtonMode", false),
            reduceAnimation = prefs.getBoolean("reduceAnimation", false),
            masterNotifications = prefs.getBoolean("masterNotifications", true),
            prayerNotifications = prefs.getBoolean("prayerNotifications", true),
            medicineReminders = prefs.getBoolean("medicineReminders", true),
            hadithNotifications = prefs.getBoolean("hadithNotifications", true),
            newsNotifications = prefs.getBoolean("newsNotifications", true),
            familyCheckIn = prefs.getBoolean("familyCheckIn", true),
            weatherAlerts = prefs.getBoolean("weatherAlerts", true),
            theme = prefs.getInt("theme", 0),
            colorTheme = prefs.getInt("colorTheme", 0),
            bengaliNumerals = prefs.getBoolean("bengaliNumerals", true),
            journalPinLock = prefs.getBoolean("journalPinLock", false),
            language = prefs.getInt("language", 0)
        )
    }

    fun updateFontSize(size: Int) {
        _state.value = _state.value.copy(fontSize = size)
        persistSettings()
    }

    fun toggleHighContrast(enabled: Boolean) {
        _state.value = _state.value.copy(highContrast = enabled)
        persistSettings()
    }

    fun toggleLargeButtonMode(enabled: Boolean) {
        _state.value = _state.value.copy(largeButtonMode = enabled)
        persistSettings()
    }

    fun toggleReduceAnimation(enabled: Boolean) {
        _state.value = _state.value.copy(reduceAnimation = enabled)
        persistSettings()
    }

    fun toggleMasterNotifications(enabled: Boolean) {
        _state.value = _state.value.copy(masterNotifications = enabled)
        persistSettings()
    }

    fun toggleNotification(type: String, enabled: Boolean) {
        when (type) {
            "prayer" -> _state.value = _state.value.copy(prayerNotifications = enabled)
            "medicine" -> _state.value = _state.value.copy(medicineReminders = enabled)
            "hadith" -> _state.value = _state.value.copy(hadithNotifications = enabled)
            "news" -> _state.value = _state.value.copy(newsNotifications = enabled)
            "family" -> _state.value = _state.value.copy(familyCheckIn = enabled)
            "weather" -> _state.value = _state.value.copy(weatherAlerts = enabled)
        }
        persistSettings()
    }

    fun setTheme(theme: Int) {
        _state.value = _state.value.copy(theme = theme)
        persistSettings()
    }

    fun setColorTheme(colorTheme: Int) {
        _state.value = _state.value.copy(colorTheme = colorTheme)
        persistSettings()
    }

    fun toggleBengaliNumerals(enabled: Boolean) {
        _state.value = _state.value.copy(bengaliNumerals = enabled)
        persistSettings()
    }

    fun toggleJournalPinLock(enabled: Boolean) {
        _state.value = _state.value.copy(journalPinLock = enabled)
        persistSettings()
    }

    fun setLanguage(language: Int) {
        _state.value = _state.value.copy(language = language)
        persistSettings()
    }

    private fun persistSettings() {
        val currentState = _state.value
        
        // Save to SharedPreferences
        prefs.edit().apply {
            putInt("fontSize", currentState.fontSize)
            putBoolean("highContrast", currentState.highContrast)
            putBoolean("largeButtonMode", currentState.largeButtonMode)
            putBoolean("reduceAnimation", currentState.reduceAnimation)
            putBoolean("masterNotifications", currentState.masterNotifications)
            putBoolean("prayerNotifications", currentState.prayerNotifications)
            putBoolean("medicineReminders", currentState.medicineReminders)
            putBoolean("hadithNotifications", currentState.hadithNotifications)
            putBoolean("newsNotifications", currentState.newsNotifications)
            putBoolean("familyCheckIn", currentState.familyCheckIn)
            putBoolean("weatherAlerts", currentState.weatherAlerts)
            putInt("theme", currentState.theme)
            putInt("colorTheme", currentState.colorTheme)
            putBoolean("bengaliNumerals", currentState.bengaliNumerals)
            putBoolean("journalPinLock", currentState.journalPinLock)
            putInt("language", currentState.language)
            apply()
        }

        // Save/Sync to local SQLite Room Profile
        viewModelScope.launch {
            try {
                val fontSizeStr = when (currentState.fontSize) {
                    0 -> "small"
                    1 -> "medium"
                    2 -> "large"
                    3 -> "xlarge"
                    else -> "medium"
                }
                val themeStr = when (currentState.theme) {
                    0 -> "system"
                    1 -> "light"
                    2 -> "dark"
                    else -> "system"
                }
                val dao = com.example.data.local.DatabaseProvider.getDatabase(getApplication()).ammuDao()
                val existing = dao.getProfileOnce()
                val updatedProfile = if (existing != null) {
                    existing.copy(
                        preferredFontSize = fontSizeStr,
                        darkModePreference = themeStr,
                        notificationEnabled = currentState.masterNotifications,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    com.example.data.local.entity.Profile(
                        id = "default_user",
                        fullName = "ব্যবহারকারী",
                        phone = "",
                        dateOfBirth = "",
                        preferredFontSize = fontSizeStr,
                        darkModePreference = themeStr,
                        notificationEnabled = currentState.masterNotifications,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                dao.insertProfile(updatedProfile)

                // Sync to Supabase
                try {
                    val currentUser = supabaseClient.auth.currentUserOrNull()
                    if (currentUser != null) {
                        val profileJson = buildJsonObject {
                            put("id", currentUser.id)
                            put("full_name", updatedProfile.fullName ?: "ব্যবহারকারী")
                            put("notification_enabled", updatedProfile.notificationEnabled)
                        }
                        supabaseClient.postgrest.from("profiles").upsert(profileJson)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateProfileInfo(fullName: String, phone: String, dateOfBirth: String, locationCity: String, avatarUrl: String) {
        viewModelScope.launch {
            try {
                val dao = com.example.data.local.DatabaseProvider.getDatabase(getApplication()).ammuDao()
                val existing = dao.getProfileOnce()
                val updatedProfile = if (existing != null) {
                    existing.copy(
                        fullName = fullName,
                        phone = phone,
                        dateOfBirth = dateOfBirth,
                        locationCity = locationCity,
                        avatarUrl = avatarUrl,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    val currentState = _state.value
                    val fontSizeStr = when (currentState.fontSize) {
                        0 -> "small"
                        1 -> "medium"
                        2 -> "large"
                        3 -> "xlarge"
                        else -> "medium"
                    }
                    val themeStr = when (currentState.theme) {
                        0 -> "system"
                        1 -> "light"
                        2 -> "dark"
                        else -> "system"
                    }
                    com.example.data.local.entity.Profile(
                        id = "default_user",
                        fullName = fullName,
                        phone = phone,
                        dateOfBirth = dateOfBirth,
                        locationCity = locationCity,
                        avatarUrl = avatarUrl,
                        preferredFontSize = fontSizeStr,
                        darkModePreference = themeStr,
                        notificationEnabled = currentState.masterNotifications,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                dao.insertProfile(updatedProfile)

                // Sync to Supabase
                try {
                    val currentUser = supabaseClient.auth.currentUserOrNull()
                    if (currentUser != null) {
                        val profileJson = buildJsonObject {
                            put("id", currentUser.id)
                            put("full_name", fullName)
                            put("notification_enabled", updatedProfile.notificationEnabled)
                        }
                        supabaseClient.postgrest.from("profiles").upsert(profileJson)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
