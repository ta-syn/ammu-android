package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.Profile
import com.example.data.local.entity.TasbihSession
import com.example.data.local.entity.FavoriteDua
import kotlinx.coroutines.flow.Flow

@Dao
interface AmmuDao {
    @Query("SELECT * FROM profiles LIMIT 1")
    fun getProfile(): Flow<Profile?>

    @Query("SELECT * FROM profiles LIMIT 1")
    suspend fun getProfileOnce(): Profile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)
    
    @Insert
    suspend fun insertTasbihSession(session: TasbihSession)

    @Query("SELECT * FROM tasbih_sessions ORDER BY createdAt DESC")
    fun getAllTasbihSessions(): Flow<List<TasbihSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteDua(favoriteDua: FavoriteDua)

    @Delete
    suspend fun deleteFavoriteDua(favoriteDua: FavoriteDua)

    @Query("SELECT duaId FROM favorite_duas")
    fun getFavoriteDuaIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerLog(log: com.example.data.local.entity.PrayerLog): Long

    @Query("SELECT * FROM prayer_logs WHERE scheduledAt >= :start AND scheduledAt <= :end ORDER BY scheduledAt ASC")
    fun getPrayerLogsForDateRange(start: Long, end: Long): Flow<List<com.example.data.local.entity.PrayerLog>>

    @Query("SELECT * FROM prayer_logs WHERE prayerName = :name AND scheduledAt >= :startOfDay AND scheduledAt <= :endOfDay LIMIT 1")
    suspend fun getPrayerLogForNameAndDate(name: String, startOfDay: Long, endOfDay: Long): com.example.data.local.entity.PrayerLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(record: com.example.data.local.entity.HealthRecord)

    @Query("SELECT * FROM health_records ORDER BY recordedAt DESC")
    fun getAllHealthRecords(): Flow<List<com.example.data.local.entity.HealthRecord>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: com.example.data.local.entity.Medicine): Long

    @Query("SELECT * FROM medicines WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveMedicines(): Flow<List<com.example.data.local.entity.Medicine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicineLog(log: com.example.data.local.entity.MedicineLog)

    @Query("SELECT * FROM medicine_logs WHERE scheduledAt >= :startOfDay AND scheduledAt <= :endOfDay ORDER BY scheduledAt ASC")
    fun getMedicineLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<com.example.data.local.entity.MedicineLog>>

    @Query("SELECT * FROM medicine_logs ORDER BY scheduledAt DESC LIMIT 100")
    fun getRecentMedicineLogs(): Flow<List<com.example.data.local.entity.MedicineLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: com.example.data.local.entity.Appointment)

    @Query("SELECT * FROM appointments ORDER BY appointmentDate DESC")
    fun getAllAppointments(): Flow<List<com.example.data.local.entity.Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: com.example.data.local.entity.Recipe)

    @Query("SELECT * FROM recipes WHERE isFavorite = 1")
    fun getFavoriteRecipes(): Flow<List<com.example.data.local.entity.Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsArticles(articles: List<com.example.data.local.entity.NewsArticle>)

    @Query("SELECT * FROM news_articles ORDER BY publishedAt DESC")
    fun getAllNews(): Flow<List<com.example.data.local.entity.NewsArticle>>

    @Query("SELECT * FROM news_articles WHERE category = :category ORDER BY publishedAt DESC")
    fun getNewsByCategory(category: String): Flow<List<com.example.data.local.entity.NewsArticle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingList(shoppingList: com.example.data.local.entity.ShoppingList): Long

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getShoppingListById(id: Int): com.example.data.local.entity.ShoppingList?

    @Query("SELECT * FROM shopping_lists ORDER BY updatedAt DESC")
    fun getAllShoppingLists(): Flow<List<com.example.data.local.entity.ShoppingList>>

    @Delete
    suspend fun deleteShoppingList(shoppingList: com.example.data.local.entity.ShoppingList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: com.example.data.local.entity.FamilyMember)

    @Query("SELECT * FROM family_members")
    fun getAllFamilyMembers(): Flow<List<com.example.data.local.entity.FamilyMember>>

    @Delete
    suspend fun deleteFamilyMember(member: com.example.data.local.entity.FamilyMember)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: com.example.data.local.entity.JournalEntry)

    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
    fun getAllJournalEntries(): Flow<List<com.example.data.local.entity.JournalEntry>>

    @Delete
    suspend fun deleteJournalEntry(entry: com.example.data.local.entity.JournalEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: com.example.data.local.entity.AppNotification)

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC LIMIT 20")
    fun getRecentNotifications(): Flow<List<com.example.data.local.entity.AppNotification>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseRecord(record: com.example.data.local.entity.ExpenseRecord)

    @Query("SELECT * FROM expense_records ORDER BY recordedAt DESC, createdAt DESC")
    fun getAllExpenseRecords(): Flow<List<com.example.data.local.entity.ExpenseRecord>>

    @Delete
    suspend fun deleteExpenseRecord(record: com.example.data.local.entity.ExpenseRecord)

    @Delete
    suspend fun deleteMedicine(medicine: com.example.data.local.entity.Medicine)

    @Query("DELETE FROM medicine_logs WHERE medicineId = :medicineId")
    suspend fun deleteLogsForMedicine(medicineId: Int)

    @Delete
    suspend fun deleteAppointment(appointment: com.example.data.local.entity.Appointment)

    @Delete
    suspend fun deleteRecipe(recipe: com.example.data.local.entity.Recipe)
}
