package com.example.ui.screens.quran

import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AyahUi(
    val number: Int,
    val globalNumber: Int,  // Global ayah number (1-6236) for audio URL
    val arabic: String,
    val translationBn: String,
    val audioUrl: String    // CDN audio URL
)

sealed class SurahReaderUiState {
    object Loading : SurahReaderUiState()
    data class Success(val surah: SurahDetailsDto, val ayahs: List<AyahUi>) : SurahReaderUiState()
    data class Error(val message: String) : SurahReaderUiState()
}

sealed class AudioState {
    object Idle : AudioState()
    data class Playing(val ayahIndex: Int) : AudioState()
    data class Paused(val ayahIndex: Int) : AudioState()
    object Loading : AudioState()
}

class SurahReaderViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SurahReaderUiState>(SurahReaderUiState.Loading)
    val uiState: StateFlow<SurahReaderUiState> = _uiState

    private val _audioState = MutableStateFlow<AudioState>(AudioState.Idle)
    val audioState: StateFlow<AudioState> = _audioState

    private var mediaPlayer: MediaPlayer? = null

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
                            globalNumber = arabicAyah.number,
                            arabic = arabicAyah.text,
                            translationBn = banglaSurah.ayahs.getOrNull(index)?.text ?: "",
                            audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${arabicAyah.number}.mp3"
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

    fun toggleAyahAudio(ayahIndex: Int, audioUrl: String) {
        val current = _audioState.value
        when {
            current is AudioState.Playing && current.ayahIndex == ayahIndex -> {
                // Same ayah playing → pause
                mediaPlayer?.pause()
                _audioState.value = AudioState.Paused(ayahIndex)
            }
            current is AudioState.Paused && current.ayahIndex == ayahIndex -> {
                // Same ayah paused → resume
                mediaPlayer?.start()
                _audioState.value = AudioState.Playing(ayahIndex)
            }
            else -> {
                // New ayah → stop previous, play new
                playAyah(ayahIndex, audioUrl)
            }
        }
    }

    private fun playAyah(ayahIndex: Int, audioUrl: String) {
        _audioState.value = AudioState.Loading
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioUrl)
                setOnPreparedListener {
                    start()
                    _audioState.value = AudioState.Playing(ayahIndex)
                }
                setOnCompletionListener {
                    _audioState.value = AudioState.Idle
                }
                setOnErrorListener { _, _, _ ->
                    _audioState.value = AudioState.Idle
                    true
                }
                prepareAsync()
            } catch (e: Exception) {
                _audioState.value = AudioState.Idle
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _audioState.value = AudioState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
