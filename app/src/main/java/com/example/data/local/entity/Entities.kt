package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey val id: String, // uuid
    val fullName: String?,
    val phone: String?, // unique
    val dateOfBirth: String?, // date string
    val locationCity: String = "Dhaka",
    val locationLat: Double?,
    val locationLng: Double?,
    val avatarUrl: String?,
    val preferredFontSize: String = "large",
    val subscriptionTier: String = "free",
    val notificationEnabled: Boolean = true,
    val darkModePreference: String = "system",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "prayer_logs")
data class PrayerLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val prayerName: String,
    val scheduledAt: Long,
    val prayedAt: Long?,
    val status: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quran_progress")
data class QuranProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val surahNumber: Int,
    val ayahNumber: Int,
    val lastReadAt: Long,
    val isBookmarked: Boolean,
    val notes: String?
)

@Entity(tableName = "tasbih_sessions")
data class TasbihSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val tasbihType: String,
    val customText: String?,
    val targetCount: Int = 33,
    val completedCount: Int,
    val completedAt: Long?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "health_records")
data class HealthRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val recordType: String,
    val valuePrimary: Double,
    val valueSecondary: Double?,
    val unit: String,
    val notes: String?,
    val recordedAt: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val medicineName: String,
    val banglaName: String?,
    val dosage: String,
    val frequency: String,
    val timesJson: String, // Store as JSON string
    val startDate: String,
    val endDate: String?,
    val notes: String?,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "medicine_logs")
data class MedicineLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val medicineId: Int,
    val scheduledAt: Long,
    val takenAt: Long?,
    val status: String
)

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val title: String,
    val mode: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val userId: String,
    val role: String,
    val content: String,
    val tokensUsed: Int?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerId: String = "default_owner",
    val memberProfileId: String? = null,
    val name: String,
    val relation: String,
    val phone: String,
    val isEmergencyContact: Boolean = false,
    val notes: String = "",
    val birthDate: String = "",
    val avatarColor: Long = 0xFF1976D2,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val title: String?,
    val content: String,
    val mood: String?,
    val photoUrlsJson: String = "[]", // Store as JSON string
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shopping_lists")
data class ShoppingList(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val title: String,
    val itemsJson: String, // Store as JSON string
    val isShared: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expense_records")
data class ExpenseRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val category: String,
    val amount: Double,
    val description: String?,
    val recordedAt: String, // Date string
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_duas")
data class FavoriteDua(
    @PrimaryKey val duaId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "default_user",
    val doctorName: String,
    val specialty: String,
    val hospitalName: String,
    val appointmentDate: Long,
    val reason: String?,
    val notes: String?,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val ingredientsList: String,
    val steps: String,
    val prepTime: String,
    val isFavorite: Boolean = false,
    val isDiabetesFriendly: Boolean = false,
    val category: String = "general"
)

@Entity(tableName = "news_articles")
data class NewsArticle(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val publishedAt: Long,
    val category: String,
    val imageUrl: String? = null,
    val url: String? = null,
    val isFavorite: Boolean = false
)

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val type: String, // "prayer", "medicine", "hadith", "family"
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
