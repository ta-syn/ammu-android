package com.example.ui.components

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.local.entity.AppNotification
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationSharedViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = DatabaseProvider.getDatabase(application).ammuDao()

    val notifications: StateFlow<List<AppNotification>> = dao.getRecentNotifications()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            dao.markNotificationAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            dao.markAllNotificationsAsRead()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            dao.deleteAllNotifications()
        }
    }

    fun simulateNewNotification(title: String, message: String, type: String) {
        val prefs = getApplication<Application>().getSharedPreferences("ammu_settings", android.content.Context.MODE_PRIVATE)
        val master = prefs.getBoolean("masterNotifications", true)
        if (!master) return
        
        val key = when (type) {
            "prayer" -> "prayerNotifications"
            "medicine" -> "medicineReminders"
            "hadith" -> "hadithNotifications"
            "news" -> "newsNotifications"
            "family" -> "familyCheckIn"
            "weather" -> "weatherAlerts"
            else -> ""
        }
        if (key.isNotEmpty() && !prefs.getBoolean(key, true)) {
            return
        }
        viewModelScope.launch {
            dao.insertNotification(AppNotification(title = title, message = message, type = type))
            com.example.ui.utils.SystemNotificationHelper.showNotification(getApplication(), title, message)
        }
    }
}
