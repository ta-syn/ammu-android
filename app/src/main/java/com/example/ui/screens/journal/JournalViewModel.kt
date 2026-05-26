package com.example.ui.screens.journal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.local.entity.JournalEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = DatabaseProvider.getDatabase(application).ammuDao()

    val entries: StateFlow<List<JournalEntry>> = dao.getAllJournalEntries()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Pre-populate with some sample mock data if empty
        viewModelScope.launch {
            dao.getAllJournalEntries().collect { currentEntries ->
                if (currentEntries.isEmpty()) {
                    dao.insertJournalEntry(
                        JournalEntry(
                            userId = "default_user",
                            title = "খুব ভালো দিন",
                            content = "আজকে নাতনিদের সাথে ভিডিও কলে কথা বললাম। ওরা অনেক বড় হয়ে গেছে। খুব ভালো লেগেছে তাদের সাথে কথা বলে। আলহামদুলিল্লাহ।",
                            mood = "😊",
                            createdAt = System.currentTimeMillis() - 86400000 * 2 // 2 days ago
                        )
                    )
                    dao.insertJournalEntry(
                        JournalEntry(
                            userId = "default_user",
                            title = "ডাক্তারের কাছে যাওয়া",
                            content = "আজ সকালে চেকআপের জন্য গিয়েছিলাম। ডাক্তার বলল প্রেসার এখন নরমাল আছে। ওষুধ নিয়মিত খেতে বলেছে।",
                            mood = "😐",
                            createdAt = System.currentTimeMillis() - 86400000 * 5 // 5 days ago
                        )
                    )
                }
                throw kotlinx.coroutines.CancellationException()
            }
        }
    }

    fun addEntry(title: String, content: String, mood: String) {
        viewModelScope.launch {
            dao.insertJournalEntry(
                JournalEntry(
                    userId = "default_user",
                    title = title.takeIf { it.isNotBlank() },
                    content = content,
                    mood = mood
                )
            )
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            dao.deleteJournalEntry(entry)
        }
    }
}
