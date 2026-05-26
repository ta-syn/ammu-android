package com.example.data.remote

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    val current_weather: CurrentWeather?,
    val hourly: HourlyForecast?,
    val daily: DailyForecast?
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int
)

@JsonClass(generateAdapter = true)
data class HourlyForecast(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val apparent_temperature: List<Double>?,
    val relative_humidity_2m: List<Int>?,
    val precipitation_probability: List<Int>?,
    val weather_code: List<Int>?
)

@JsonClass(generateAdapter = true)
data class DailyForecast(
    val time: List<String>,
    val weather_code: List<Int>?,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>
)

interface WeatherService {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double = 23.8103, // Dhaka
        @Query("longitude") longitude: Double = 90.4125,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("hourly") hourly: String = "temperature_2m,apparent_temperature,relative_humidity_2m,precipitation_probability,weather_code",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}
