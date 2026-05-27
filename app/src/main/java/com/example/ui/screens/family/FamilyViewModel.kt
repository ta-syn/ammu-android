package com.example.ui.screens.family

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.local.entity.FamilyMember
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FamilyViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = DatabaseProvider.getDatabase(application).ammuDao()

    val familyMembers: StateFlow<List<FamilyMember>> = dao.getAllFamilyMembers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addFamilyMember(id: Int = 0, name: String, relation: String, phone: String, isEmergency: Boolean, birthDate: String, notes: String, color: Long) {
        viewModelScope.launch {
            dao.insertFamilyMember(
                FamilyMember(
                    id = id,
                    ownerId = "default_owner",
                    name = name,
                    relation = relation,
                    phone = phone,
                    isEmergencyContact = isEmergency,
                    birthDate = birthDate,
                    notes = notes,
                    avatarColor = color
                )
            )
        }
    }

    fun deleteMember(member: FamilyMember) {
        viewModelScope.launch {
            dao.deleteFamilyMember(member)
        }
    }
}
