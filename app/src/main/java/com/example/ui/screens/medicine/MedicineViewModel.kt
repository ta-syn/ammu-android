package com.example.ui.screens.medicine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.local.entity.Medicine
import com.example.data.local.entity.MedicineLog
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.example.network.supabaseClient
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MedicineViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = DatabaseProvider.getDatabase(application).ammuDao()

    val activeMedicines: StateFlow<List<Medicine>> = dao.getActiveMedicines()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentLogs: StateFlow<List<MedicineLog>> = dao.getRecentMedicineLogs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addMedicine(
        name: String,
        banglaName: String?,
        dosage: String,
        frequency: String,
        timesJson: String,
        startDate: String,
        endDate: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val med = Medicine(
                userId = "default_user",
                medicineName = name,
                banglaName = banglaName,
                dosage = dosage,
                frequency = frequency,
                timesJson = timesJson,
                startDate = startDate,
                endDate = endDate,
                notes = notes
            )
            val insertedId = dao.insertMedicine(med).toInt()

            // Sync to Supabase
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                if (currentUser != null) {
                    val medJson = buildJsonObject {
                        put("id", insertedId)
                        put("user_id", currentUser.id)
                        put("medicine_name", name)
                        put("times_json", timesJson)
                        put("is_active", true)
                    }
                    supabaseClient.postgrest.from("medicines").upsert(medJson)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Generate scheduled logs for the next 7 days
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val todayStart = cal.timeInMillis
            
            val hourOffsets = when (frequency) {
                "১বার" -> listOf(9)
                "২বার" -> listOf(9, 21)
                "৩বার" -> listOf(8, 14, 21)
                else -> emptyList()
            }
            
            if (hourOffsets.isNotEmpty()) {
                for (dayOffset in 0 until 7) {
                    for (hour in hourOffsets) {
                        val scheduledCal = Calendar.getInstance()
                        scheduledCal.timeInMillis = todayStart
                        scheduledCal.add(Calendar.DAY_OF_YEAR, dayOffset)
                        scheduledCal.set(Calendar.HOUR_OF_DAY, hour)
                        
                        if (scheduledCal.timeInMillis >= System.currentTimeMillis()) {
                            dao.insertMedicineLog(
                                MedicineLog(
                                    userId = "default_user",
                                    medicineId = insertedId,
                                    scheduledAt = scheduledCal.timeInMillis,
                                    takenAt = null,
                                    status = "upcoming"
                                )
                            )
                        }
                    }
                }
            } else {
                // "প্রয়োজনে" (PRN) -> add 1 immediate upcoming log
                val log = MedicineLog(
                    userId = "default_user",
                    medicineId = insertedId,
                    scheduledAt = System.currentTimeMillis(),
                    takenAt = null,
                    status = "upcoming"
                )
                dao.insertMedicineLog(log)
            }
        }
    }

    fun markLogAsTaken(log: MedicineLog) {
        viewModelScope.launch {
            val updatedLog = log.copy(status = "taken", takenAt = System.currentTimeMillis())
            dao.insertMedicineLog(updatedLog)
        }
    }

    fun addManualLogForMedicine(medicineId: Int, offsetHours: Int) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.add(Calendar.HOUR, offsetHours)
            val scheduledTime = cal.timeInMillis
            val log = MedicineLog(
                userId = "default_user",
                medicineId = medicineId,
                scheduledAt = scheduledTime,
                takenAt = null,
                status = "upcoming"
            )
            dao.insertMedicineLog(log)
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            dao.deleteLogsForMedicine(medicine.id)
            dao.deleteMedicine(medicine)

            // Sync to Supabase
            try {
                supabaseClient.postgrest.from("medicines").delete {
                    filter {
                        eq("id", medicine.id)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
