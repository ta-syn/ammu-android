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

    init {
        // Mock sending initial notifications for testing
        viewModelScope.launch {
            val prefs = application.getSharedPreferences("ammu_settings", android.content.Context.MODE_PRIVATE)
            val master = prefs.getBoolean("masterNotifications", true)
            if (master) {
                dao.getRecentNotifications().collect { notifs ->
                    if (notifs.isEmpty()) {
                        if (prefs.getBoolean("prayerNotifications", true)) {
                            dao.insertNotification(
                                AppNotification(
                                    title = "🕌 জোহরের নামাজের সময় হয়েছে",
                                    message = "আস-সালামু আলাইকুম। নামাজ পড়ে নিন।",
                                    type = "prayer",
                                    createdAt = System.currentTimeMillis() - 1000 * 60 * 30
                                )
                            )
                        }
                        if (prefs.getBoolean("medicineReminders", true)) {
                            dao.insertNotification(
                                AppNotification(
                                    title = "💊 ওষুধ রিমাইন্ডার",
                                    message = "মেটফরমিন খাওয়ার সময় হয়েছে।",
                                    type = "medicine",
                                    createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 2
                                )
                            )
                        }
                        if (prefs.getBoolean("hadithNotifications", true)) {
                            dao.insertNotification(
                                AppNotification(
                                    title = "📖 আজকের হাদিস",
                                    message = "যে ব্যক্তি জ্ঞান অর্জনের জন্য কোনো পথ অবলম্বন করে, আল্লাহ তার জন্য জান্নাতের পথ সহজ করে দেন।",
                                    type = "hadith",
                                    createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 5
                                )
                            )
                        }
                    }
                    throw kotlinx.coroutines.CancellationException()
                }
            }
        }
    }

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
        }
    }
}
