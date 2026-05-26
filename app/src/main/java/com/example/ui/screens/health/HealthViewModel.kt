package com.example.ui.screens.health

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.local.entity.HealthRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = DatabaseProvider.getDatabase(application).ammuDao()

    val records: StateFlow<List<HealthRecord>> = dao.getAllHealthRecords()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addRecord(type: String, valuePrimary: Double, valueSecondary: Double? = null, unit: String, notes: String? = null) {
        viewModelScope.launch {
            val newRecord = HealthRecord(
                userId = "default_user",
                recordType = type,
                valuePrimary = valuePrimary,
                valueSecondary = valueSecondary,
                unit = unit,
                notes = notes,
                recordedAt = System.currentTimeMillis()
            )
            dao.insertHealthRecord(newRecord)
        }
    }
}

