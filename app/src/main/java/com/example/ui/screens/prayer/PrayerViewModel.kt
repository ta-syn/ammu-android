package com.example.ui.screens.prayer

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.local.entity.PrayerLog
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

class PrayerViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = DatabaseProvider.getDatabase(application).ammuDao()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _locationName = MutableStateFlow("ঢাকা, বাংলাদেশ (ডিফল্ট)")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    private val _latitude = MutableStateFlow(23.8103)
    private val _longitude = MutableStateFlow(90.4125)

    private val _todayPrayerTimes = MutableStateFlow<Map<String, String>>(emptyMap())
    val todayPrayerTimes: StateFlow<Map<String, String>> = _todayPrayerTimes.asStateFlow()

    private val _todayLogs = MutableStateFlow<List<PrayerLog>>(emptyList())
    val todayLogs: StateFlow<List<PrayerLog>> = _todayLogs.asStateFlow()

    private val _weeklyLogs = MutableStateFlow<List<PrayerLog>>(emptyList())
    val weeklyLogs: StateFlow<List<PrayerLog>> = _weeklyLogs.asStateFlow()

    init {
        calculateTimes()
        loadTodayLogs()
        loadWeeklyLogs()
    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    _latitude.value = location.latitude
                    _longitude.value = location.longitude
                    _locationName.value = "বর্তমান অবস্থান"
                    calculateTimes()
                    loadTodayLogs()
                    loadWeeklyLogs()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateTimes() {
        val cal = Calendar.getInstance()
        val tz = TimeZone.getDefault()
        val offset = tz.getOffset(cal.timeInMillis) / (1000.0 * 60.0 * 60.0)
        _todayPrayerTimes.value = PrayerTimeCalculator.calculateTimes(
            _latitude.value,
            _longitude.value,
            offset,
            cal
        )
    }

    private fun loadTodayLogs() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis
            
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val endOfDay = cal.timeInMillis

            dao.getPrayerLogsForDateRange(startOfDay, endOfDay).collect { logs ->
                _todayLogs.value = logs
            }
        }
    }

    private fun loadWeeklyLogs() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -6)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val start = cal.timeInMillis

            val calEnd = Calendar.getInstance()
            calEnd.set(Calendar.HOUR_OF_DAY, 23)
            calEnd.set(Calendar.MINUTE, 59)
            calEnd.set(Calendar.SECOND, 59)
            val end = calEnd.timeInMillis

            dao.getPrayerLogsForDateRange(start, end).collect { logs ->
                _weeklyLogs.value = logs
            }
        }
    }

    fun togglePrayer(prayerName: String, scheduledTimeStr: String) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val endOfDay = cal.timeInMillis

            val existing = dao.getPrayerLogForNameAndDate(prayerName, startOfDay, endOfDay)
            if (existing != null) {
                // Delete or toggle to missed
                val updated = existing.copy(
                    status = if (existing.status == "prayed") "missed" else "prayed",
                    prayedAt = if (existing.status == "prayed") null else System.currentTimeMillis()
                )
                dao.insertPrayerLog(updated)
            } else {
                // Parse the scheduled time string into millisecond timestamp
                val scheduledTimestamp = parseTimeStringToMillis(scheduledTimeStr)
                val newLog = PrayerLog(
                    userId = "default_user",
                    prayerName = prayerName,
                    scheduledAt = scheduledTimestamp,
                    prayedAt = System.currentTimeMillis(),
                    status = "prayed"
                )
                dao.insertPrayerLog(newLog)
            }
        }
    }

    private fun parseTimeStringToMillis(timeStr: String): Long {
        return try {
            val cal = Calendar.getInstance()
            val cleanStr = timeStr.replace(" AM", "").replace(" PM", "").trim()
            val parts = cleanStr.split(":")
            var hour = parts[0].toInt()
            val min = parts[1].toInt()
            
            if (timeStr.contains("PM") && hour != 12) {
                hour += 12
            } else if (timeStr.contains("AM") && hour == 12) {
                hour = 0
            }
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, min)
            cal.set(Calendar.SECOND, 0)
            cal.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}

object PrayerTimeCalculator {
    private const val FAJR_ANGLE = -18.0
    private const val ISHA_ANGLE = -18.0

    fun calculateTimes(latitude: Double, longitude: Double, timezoneOffset: Double, date: Calendar): Map<String, String> {
        val dayOfYear = date.get(Calendar.DAY_OF_YEAR)
        val b = 2.0 * Math.PI * (dayOfYear - 81) / 365.0
        val eqt = 9.87 * sin(2.0 * b) - 7.53 * cos(b) - 1.5 * sin(b)
        val declination = 23.45 * sin(2.0 * Math.PI * (dayOfYear - 80) / 365.0)
        
        val noonUtc = 12.0 - (longitude / 15.0) - (eqt / 60.0)
        val noonLocal = noonUtc + timezoneOffset
        
        val decRad = Math.toRadians(declination)
        val latRad = Math.toRadians(latitude)
        
        val cosH0 = (sin(Math.toRadians(-0.833)) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val h0 = if (cosH0 in -1.0..1.0) Math.toDegrees(acos(cosH0)) else 90.0
        val sunset = noonLocal + (h0 / 15.0)
        
        val cosHf = (sin(Math.toRadians(FAJR_ANGLE)) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val hf = if (cosHf in -1.0..1.0) Math.toDegrees(acos(cosHf)) else 90.0
        val fajr = noonLocal - (hf / 15.0)
        
        val cosHi = (sin(Math.toRadians(ISHA_ANGLE)) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val hi = if (cosHi in -1.0..1.0) Math.toDegrees(acos(cosHi)) else 90.0
        val isha = noonLocal + (hi / 15.0)
        
        val g = abs(latitude - declination)
        val cotAsr = 1.0 + tan(Math.toRadians(g))
        val asrAngle = Math.toDegrees(atan(1.0 / cotAsr))
        val cosHa = (sin(Math.toRadians(asrAngle)) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val ha = if (cosHa in -1.0..1.0) Math.toDegrees(acos(cosHa)) else 90.0
        val asr = noonLocal + (ha / 15.0)
        
        return mapOf(
            "fajr" to formatTime(fajr),
            "dhuhr" to formatTime(noonLocal + 5.0 / 60.0),
            "asr" to formatTime(asr),
            "maghrib" to formatTime(sunset + 2.0 / 60.0),
            "isha" to formatTime(isha)
        )
    }

    private fun formatTime(hours: Double): String {
        var h = hours
        if (h.isNaN() || h.isInfinite()) return "12:00 PM"
        
        h = (h + 24.0) % 24.0
        val hour = h.toInt()
        val minutes = ((h - hour) * 60.0).roundToInt()
        
        var finalMin = minutes
        var finalHour = hour
        if (finalMin >= 60) {
            finalMin -= 60
            finalHour += 1
        }
        
        val displayHour = if (finalHour % 12 == 0) 12 else finalHour % 12
        val ampm = if (finalHour < 12 || finalHour == 24) "AM" else "PM"
        val minStr = String.format("%02d", finalMin)
        
        return "$displayHour:$minStr $ampm"
    }
}
