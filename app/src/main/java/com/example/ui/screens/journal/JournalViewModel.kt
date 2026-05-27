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
