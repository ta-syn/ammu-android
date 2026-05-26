package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.dao.AmmuDao
import com.example.data.local.entity.*

@Database(
    entities = [
        Profile::class,
        PrayerLog::class,
        QuranProgress::class,
        TasbihSession::class,
        HealthRecord::class,
        Medicine::class,
        MedicineLog::class,
        ChatSession::class,
        ChatMessage::class,
        FamilyMember::class,
        JournalEntry::class,
        ShoppingList::class,
        ExpenseRecord::class,
        FavoriteDua::class,
        Appointment::class,
        Recipe::class,
        NewsArticle::class,
        AppNotification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AmmuDatabase : RoomDatabase() {
    abstract fun ammuDao(): AmmuDao
}
