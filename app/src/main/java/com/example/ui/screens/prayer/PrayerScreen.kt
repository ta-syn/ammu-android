package com.example.ui.screens.prayer

import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.PrayerLog
import com.example.ui.components.*
import com.example.ui.theme.DangerSoft
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GreenLight
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.util.Calendar

enum class PrayerStatus { PRAYED, PENDING, MISSED }

data class PrayerUiModel(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val nameAr: String,
    val time: String,
    val status: PrayerStatus,
    val isNext: Boolean = false
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PrayerScreen(viewModel: PrayerViewModel = viewModel()) {
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            viewModel.requestLocation()
        }
    }

    val todayTimes by viewModel.todayPrayerTimes.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()
    val weeklyLogs by viewModel.weeklyLogs.collectAsState()
    val locationName by viewModel.locationName.collectAsState()

    val prayerKeys = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    val prayerNamesBn = mapOf("fajr" to "ফজর", "dhuhr" to "যোহর", "asr" to "আসর", "maghrib" to "মাগরিব", "isha" to "এশা")
    val prayerNamesAr = mapOf("fajr" to "الفجر", "dhuhr" to "الظهر", "asr" to "العصر", "maghrib" to "المغرب", "isha" to "العشاء")
    val prayerNamesEn = mapOf("fajr" to "Fajr", "dhuhr" to "Dhuhr", "asr" to "Asr", "maghrib" to "Maghrib", "isha" to "Isha")

    // Determine the next prayer
    val currentTime = System.currentTimeMillis()
    val parsedTimes = todayTimes.mapValues { (_, timeStr) ->
        parseTimeToMillis(timeStr)
    }

    var nextPrayerKey = "fajr"
    var minDiff = Long.MAX_VALUE
    parsedTimes.forEach { (key, timeMs) ->
        val diff = timeMs - currentTime
        if (diff > 0 && diff < minDiff) {
            minDiff = diff
            nextPrayerKey = key
        }
    }

    val prayersList = prayerKeys.map { key ->
        val timeStr = todayTimes[key] ?: "12:00 PM"
        val log = todayLogs.find { it.prayerName == key }
        val status = when (log?.status) {
            "prayed" -> PrayerStatus.PRAYED
            "missed" -> PrayerStatus.MISSED
            else -> PrayerStatus.PENDING
        }
        
        PrayerUiModel(
            id = key,
            nameEn = prayerNamesEn[key] ?: key,
            nameBn = prayerNamesBn[key] ?: key,
            nameAr = prayerNamesAr[key] ?: "",
            time = toBengaliTime(timeStr),
            status = status,
            isNext = key == nextPrayerKey
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Next Prayer Hero
        CardIslamic(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                val nextPrayerBn = prayerNamesBn[nextPrayerKey] ?: ""
                val schedMs = parsedTimes[nextPrayerKey] ?: System.currentTimeMillis()
                val diffMs = schedMs - currentTime
                val countdownText = if (diffMs > 0) {
                    val diffMin = (diffMs / (1000 * 60)) % 60
                    val diffHour = (diffMs / (1000 * 60 * 60))
                    val hourText = if (diffHour > 0) "${toBengaliDigits(diffHour.toString())} ঘণ্টা " else ""
                    "$hourText${toBengaliDigits(diffMin.toString())} মিনিট বাকি"
                } else {
                    "সময় হয়েছে"
                }
                
                BanglaText(text = "পরবর্তী নামাজ (${locationName})", color = MaterialTheme.colorScheme.onSurfaceVariant)
                BanglaHeading(text = nextPrayerBn, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                BanglaHeading(text = countdownText, fontWeight = FontWeight.Bold)
            }
        }

        // Location permission trigger
        if (!locationPermissions.allPermissionsGranted) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BanglaText(text = "সঠিক নামাজের সময়ের জন্য লোকেশন পারমিশন দিন", modifier = Modifier.weight(1f))
                    Button(
                        onClick = { locationPermissions.launchMultiplePermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        BanglaText("অনুমতি দিন", color = Color.White)
                    }
                }
            }
        }

        // Section A: Today's Prayers
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BanglaHeading(text = "আজকের নামাজ", fontSize = 20.sp)
            prayersList.forEach { prayer ->
                PrayerCard(prayer, onToggle = {
                    viewModel.togglePrayer(prayer.id, todayTimes[prayer.id] ?: "12:00 PM")
                })
            }
        }

        // Section B: Tracker
        TrackerSection(weeklyLogs)

        // Section C: Adhan Player
        AdhanSection()

        // Section D: Notification Settings
        NotificationSettingsSection(prayersList)
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun PrayerCard(prayer: PrayerUiModel, onToggle: () -> Unit) {
    val borderColor = when (prayer.status) {
        PrayerStatus.PRAYED -> GreenLight
        PrayerStatus.PENDING -> if (prayer.isNext) GoldAccent else Color.Transparent
        PrayerStatus.MISSED -> DangerSoft
    }

    val bgColor = if (prayer.isNext) GoldAccent.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface

    Card(
        shape = RoundedCornerShape(Radius.md),
        border = BorderStroke(if (prayer.isNext || prayer.status == PrayerStatus.PRAYED) 1.dp else 0.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = prayer.nameAr, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    BanglaHeading(text = prayer.nameBn, fontSize = 18.sp)
                }
                Text(text = prayer.time, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }

            if (prayer.status == PrayerStatus.PRAYED) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(GreenLight.copy(alpha = 0.2f), CircleShape)
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "Prayed", tint = GreenLight)
                }
            } else {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clickable { onToggle() }
                ) {
                    BanglaText(
                        text = "পড়েছি",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TrackerSection(weeklyLogs: List<PrayerLog>) {
    CardBase(modifier = Modifier.fillMaxWidth()) {
        Column {
            BanglaHeading(text = "সাপ্তাহিক ট্র্যাকার", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val daysOfWeekBn = listOf("শনি", "রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র")
                
                for (dayOffset in 0 until 7) {
                    val dayCal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -6 + dayOffset)
                    }
                    val dayNameBn = daysOfWeekBn.getOrNull((dayCal.get(Calendar.DAY_OF_WEEK) + 5) % 7) ?: ""
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BanglaText(text = dayNameBn, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val prayerKeys = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
                        prayerKeys.forEach { key ->
                            val wasPrayed = weeklyLogs.any { log ->
                                log.prayerName == key &&
                                log.status == "prayed" &&
                                isSameDay(log.scheduledAt, dayCal.timeInMillis)
                            }
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .size(16.dp)
                                    .background(
                                        color = if (wasPrayed) GreenLight else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            val prayedCount = weeklyLogs.count { it.status == "prayed" }
            val countBn = toBengaliDigits(prayedCount.toString())
            BanglaText(
                text = "আলহামদুলিল্লাহ! এই সপ্তাহে আপনি $countBn বার নামাজ পড়েছেন 🌟",
                color = GreenLight,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AdhanSection() {
    CardBase(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play Adhan", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    BanglaHeading(text = "আযান শুনুন", fontSize = 18.sp)
                    BanglaText(text = "মিশারি রাশিদ আল-আফাসি", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                BanglaText(text = "ফজরের আযানে জাগুন", fontSize = 16.sp)
                val checkState = remember { mutableStateOf(true) }
                Switch(checked = checkState.value, onCheckedChange = { checkState.value = it })
            }
        }
    }
}

@Composable
fun NotificationSettingsSection(prayers: List<PrayerUiModel>) {
    CardBase(modifier = Modifier.fillMaxWidth()) {
        Column {
            BanglaHeading(text = "নোটিফিকেশন সেটিংস", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            prayers.forEach { prayer ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BanglaText(text = prayer.nameBn, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BanglaText(text = "১৫ মিনিট আগে", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        val isNotifyOn = remember { mutableStateOf(true) }
                        IconButton(onClick = { isNotifyOn.value = !isNotifyOn.value }) {
                            Icon(
                                imageVector = if (isNotifyOn.value) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                                contentDescription = "Notification Toggle",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun parseTimeToMillis(timeStr: String): Long {
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
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

private fun toBengaliDigits(number: String): String {
    val bengaliDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val builder = StringBuilder()
    for (char in number) {
        if (char in '0'..'9') {
            builder.append(bengaliDigits[char - '0'])
        } else {
            builder.append(char)
        }
    }
    return builder.toString()
}

private fun toBengaliTime(timeStr: String): String {
    val clean = toBengaliDigits(timeStr)
    return clean.replace("AM", "পূর্বাহ্ণ").replace("PM", "অপরাহ্ণ")
}

private fun isSameDay(ms1: Long, ms2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = ms1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = ms2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
