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

    init {
        // Prepopulate with mock data if empty initially
        viewModelScope.launch {
            dao.getAllFamilyMembers().collect { members ->
                if (members.isEmpty()) {
                    dao.insertFamilyMember(
                        FamilyMember(
                            name = "রাব্বি",
                            relation = "ছেলে",
                            phone = "01700000000",
                            isEmergencyContact = true,
                            notes = "অফিস টাইমে শুধু মেসেজ দিন",
                            birthDate = "১২ অক্টোবর",
                            avatarColor = 0xFF4CAF50
                        )
                    )
                    dao.insertFamilyMember(
                        FamilyMember(
                            name = "সুমি",
                            relation = "মেয়ে",
                            phone = "01800000000",
                            isEmergencyContact = true,
                            notes = "বিকেলে ফ্রি থাকে",
                            birthDate = "৫ মার্চ",
                            avatarColor = 0xFFE91E63
                        )
                    )
                }
                throw kotlinx.coroutines.CancellationException()
            }
        }
    }

    fun addFamilyMember(name: String, relation: String, phone: String, isEmergency: Boolean, birthDate: String, notes: String, color: Long) {
        viewModelScope.launch {
            dao.insertFamilyMember(
                FamilyMember(
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
