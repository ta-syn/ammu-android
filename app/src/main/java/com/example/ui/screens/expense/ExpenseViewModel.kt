package com.example.ui.screens.expense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.local.entity.ExpenseRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = DatabaseProvider.getDatabase(application).ammuDao()

    val expenses: StateFlow<List<ExpenseRecord>> = dao.getAllExpenseRecords()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addExpense(category: String, amount: Double, description: String, date: String = getCurrentDate()) {
        viewModelScope.launch {
            val expense = ExpenseRecord(
                userId = "master_user",
                category = category,
                amount = amount,
                description = description.takeIf { it.isNotBlank() },
                recordedAt = date
            )
            dao.insertExpenseRecord(expense)
        }
    }

    fun deleteExpense(expense: ExpenseRecord) {
        viewModelScope.launch {
            dao.deleteExpenseRecord(expense)
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
