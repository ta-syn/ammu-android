package com.example.ui.screens.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AyahUi(
    val number: Int,
    val arabic: String,
    val translationBn: String
)

sealed class SurahReaderUiState {
    object Loading : SurahReaderUiState()
    data class Success(val surah: SurahDetailsDto, val ayahs: List<AyahUi>) : SurahReaderUiState()
    data class Error(val message: String) : SurahReaderUiState()
}

class SurahReaderViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SurahReaderUiState>(SurahReaderUiState.Loading)
    val uiState: StateFlow<SurahReaderUiState> = _uiState
    
    fun fetchSurah(number: Int) {
        viewModelScope.launch {
            _uiState.value = SurahReaderUiState.Loading
            try {
                val response = AlQuranClient.service.getSurahEditions(number)
                val editions = response.data
                if (editions.size == 2) {
                    val arabicSurah = editions[0]
                    val banglaSurah = editions[1]
                    
                    val ayahsUi = arabicSurah.ayahs.mapIndexed { index, arabicAyah ->
                        AyahUi(
                            number = arabicAyah.numberInSurah,
                            arabic = arabicAyah.text,
                            translationBn = banglaSurah.ayahs.getOrNull(index)?.text ?: ""
                        )
                    }
                    _uiState.value = SurahReaderUiState.Success(arabicSurah, ayahsUi)
                } else {
                     _uiState.value = SurahReaderUiState.Error("তথ্য লোড করতে সমস্যা হচ্ছে।")
                }
            } catch (e: Exception) {
                _uiState.value = SurahReaderUiState.Error("ইন্টারনেট সংযোগ যাচাই করুন।")
            }
        }
    }
}
