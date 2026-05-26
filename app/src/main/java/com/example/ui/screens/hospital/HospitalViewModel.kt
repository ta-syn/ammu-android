package com.example.ui.screens.hospital

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.local.entity.Appointment
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HospitalViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = DatabaseProvider.getDatabase(application).ammuDao()

    val appointments: StateFlow<List<Appointment>> = dao.getAllAppointments()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addAppointment(
        doctorName: String,
        specialty: String,
        hospitalName: String,
        appointmentDate: Long,
        reason: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val appointment = Appointment(
                userId = "default_user",
                doctorName = doctorName,
                specialty = specialty,
                hospitalName = hospitalName,
                appointmentDate = appointmentDate,
                reason = reason,
                notes = notes
            )
            dao.insertAppointment(appointment)
        }
    }
}
