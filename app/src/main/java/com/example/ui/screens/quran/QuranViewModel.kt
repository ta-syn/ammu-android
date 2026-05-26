package com.example.ui.screens.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class QuranUiState {
    object Loading : QuranUiState()
    data class Success(val surahs: List<SurahDto>) : QuranUiState()
    data class Error(val message: String) : QuranUiState()
}

class QuranViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<QuranUiState>(QuranUiState.Loading)
    val uiState: StateFlow<QuranUiState> = _uiState
    
    init {
        fetchSurahs()
    }
    
    fun fetchSurahs() {
        viewModelScope.launch {
            _uiState.value = QuranUiState.Loading
            try {
                val response = AlQuranClient.service.getSurahs()
                _uiState.value = QuranUiState.Success(response.data)
            } catch (e: Exception) {
                _uiState.value = QuranUiState.Error("ইন্টারনেট সংযোগ যাচাই করুন।")
            }
        }
    }
}
