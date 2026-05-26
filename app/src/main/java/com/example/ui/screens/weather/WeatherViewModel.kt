package com.example.ui.screens.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.OpenMeteoResponse
import com.example.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(
        val weather: OpenMeteoResponse,
        val locationInfo: String = "ঢাকা, বাংলাদেশ"
    ) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        fetchWeather()
    }

    fun fetchWeather(latitude: Double = 23.8103, longitude: Double = 90.4125, locationName: String = "ঢাকা, বাংলাদেশ") {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val response = RetrofitClient.weatherService.getWeather(latitude = latitude, longitude = longitude)
                _uiState.value = WeatherUiState.Success(weather = response, locationInfo = locationName)
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("আবহাওয়ার তথ্য আনা যায়নি। কানেকশন চেক করুন।")
            }
        }
    }

    fun getWeatherEmojiAndDesc(code: Int): Pair<String, String> {
        return when (code) {
            0 -> "☀️" to "পরিষ্কার আকাশ"
            1, 2, 3 -> "⛅" to "আংশিক মেঘলা"
            45, 48 -> "🌫️" to "কুয়াশা"
            51, 53, 55 -> "🌧️" to "হালকা গুঁড়ি গুঁড়ি বৃষ্টি"
            61, 63, 65 -> "🌧️" to "বৃষ্টি"
            71, 73, 75 -> "❄️" to "তুষারপাত"
            95, 96, 99 -> "⛈️" to "বজ্রসহ বৃষ্টি"
            else -> "🌤️" to "সাধারণ আবহাওয়া"
        }
    }
}
