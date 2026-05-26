package com.example.ui.screens.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.remote.OpenMeteoResponse
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.components.shimmerEffect
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    var bgColor = Color(0xFFF9FAFB)
    var textColor = Color.Black
    
    if (uiState is WeatherUiState.Success) {
        val weatherCode = (uiState as WeatherUiState.Success).weather.current_weather?.weathercode ?: 0
        if (weatherCode == 0) { // Sunny
            bgColor = Color(0xFFFFF8E1) // warm golden
            textColor = Color(0xFF5D4037)
        } else if (weatherCode in listOf(51, 53, 55, 61, 63, 65, 95, 96, 99)) { // Rain
            bgColor = Color(0xFFE3F2FD) // cool blue
            textColor = Color(0xFF0D47A1)
        } else { // Cloud/Default
            bgColor = Color(0xFFF9FAFB)
            textColor = Color.DarkGray
        }
    }

    Scaffold(
        containerColor = bgColor
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is WeatherUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title skeleton
                        Box(modifier = Modifier.width(150.dp).height(30.dp).shimmerEffect().background(Color.LightGray, RoundedCornerShape(4.dp)))
                        Box(modifier = Modifier.width(200.dp).height(16.dp).shimmerEffect().background(Color.LightGray, RoundedCornerShape(4.dp)))
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Current weather skeleton
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(modifier = Modifier.size(80.dp).shimmerEffect().background(Color.LightGray, CircleShape))
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.width(100.dp).height(60.dp).shimmerEffect().background(Color.LightGray, RoundedCornerShape(8.dp)))
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // 3 info boxes skeleton
                            Surface(
                                color = Color.White.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(80.dp).shimmerEffect()
                            ) {}
                        }
                    }
                }
                is WeatherUiState.Error -> {
                    BanglaText(
                        (uiState as WeatherUiState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is WeatherUiState.Success -> {
                    val weather = (uiState as WeatherUiState.Success).weather
                    WeatherContent(
                        weather = weather,
                        location = (uiState as WeatherUiState.Success).locationInfo,
                        viewModel = viewModel,
                        textColor = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherContent(
    weather: OpenMeteoResponse,
    location: String,
    viewModel: WeatherViewModel,
    textColor: Color
) {
    val current = weather.current_weather
    val (emoji, desc) = viewModel.getWeatherEmojiAndDesc(current?.weathercode ?: 0)
    val temp = current?.temperature?.toInt() ?: 0

    val severAlert = current?.weathercode in listOf(95, 96, 99)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BanglaHeading("আবহাওয়া", fontSize = 24.sp, color = textColor)
            BanglaText("$location • ${getCurrentTimeStr()}", color = textColor.copy(alpha = 0.7f))
        }

        if (severAlert) {
            item {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        BanglaText("সতর্কতা: বজ্রসহ ভারী বৃষ্টির সম্ভাবনা রয়েছে। নিরাপদ স্থানে থাকুন।", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Current weather
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(emoji, fontSize = 80.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    BanglaHeading("${banglaNumerals(temp)}", fontSize = 64.sp, color = textColor)
                    BanglaHeading("°C", fontSize = 24.sp, color = textColor, modifier = Modifier.padding(top = 12.dp))
                }
                BanglaHeading(desc, fontSize = 20.sp, color = textColor)
                
                Spacer(modifier = Modifier.height(16.dp))
                val apparentTemp = weather.hourly?.apparent_temperature?.firstOrNull()?.toInt() ?: temp
                val humidity = weather.hourly?.relative_humidity_2m?.firstOrNull() ?: 0
                val wind = current?.windspeed ?: 0.0

                Surface(
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeatherDetailInfo("অনুভূত", "${banglaNumerals(apparentTemp)}°C", textColor)
                        WeatherDetailInfo("আর্দ্রতা", "${banglaNumerals(humidity)}%", textColor)
                        WeatherDetailInfo("বাতাস", "${banglaNumerals(wind.toInt())} কি.মি/ঘ", textColor)
                    }
                }
            }
        }

        // AI Advice
        item {
            Surface(
                color = GreenLight.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    BanglaHeading("💡 আম্মুর আবহাওয়া পরামর্শ", fontSize = 16.sp, color = GreenPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val wearAdv = if (temp > 30) "হালকা সুতির কাপড় পরুন।" else if (temp < 20) "হালকা শীতের কাপড় পরুন।" else "স্বাভাবিক আরামদায়ক কাপড় পরুন।"
                    val rainAdv = if (desc.contains("বৃষ্টি")) "বৃষ্টির সম্ভাবনা রয়েছে — বাইরে যাওয়ার আগে ছাতা নিন।" else "আবহাওয়া ভালো, বাইরে যাওয়ার জন্য উত্তম সময়।"

                    BanglaText("আজ কী পরবেন? $wearAdv", color = textColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    BanglaText("বাইরে যাওয়া কি ঠিক হবে? $rainAdv", color = textColor)
                }
            }
        }

        // Hourly
        item {
            BanglaHeading("প্রতি ঘণ্টার পূর্বাভাস", fontSize = 18.sp, color = textColor)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val times = weather.hourly?.time ?: emptyList()
                val temps = weather.hourly?.temperature_2m ?: emptyList()
                val codes = weather.hourly?.weather_code ?: emptyList()
                
                items(minOf(24, times.size)) { idx -> // First 24 hours
                    val timeStr = formatHour(times[idx])
                    val (hEmoji, _) = viewModel.getWeatherEmojiAndDesc(codes.getOrNull(idx) ?: 0)
                    val hTemp = temps.getOrNull(idx)?.toInt() ?: 0

                    Surface(
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BanglaText(timeStr, fontSize = 14.sp)
                            Text(hEmoji, fontSize = 24.sp, modifier = Modifier.padding(vertical = 4.dp))
                            BanglaHeading("${banglaNumerals(hTemp)}°", fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // Daily
        item {
            BanglaHeading("৭ দিনের পূর্বাভাস", fontSize = 18.sp, color = textColor)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val dTimes = weather.daily?.time ?: emptyList()
                    val count = dTimes.size
                    for (i in 0 until count) {
                        val dTime = formatDay(dTimes[i])
                        val maxT = weather.daily?.temperature_2m_max?.getOrNull(i)?.toInt() ?: 0
                        val minT = weather.daily?.temperature_2m_min?.getOrNull(i)?.toInt() ?: 0
                        val (dEmoji, dDesc) = viewModel.getWeatherEmojiAndDesc(weather.daily?.weather_code?.getOrNull(i) ?: 0)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BanglaText(dTime, modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text(dEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                BanglaText(dDesc, color = Color.Gray, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                                BanglaHeading("${banglaNumerals(maxT)}°", fontSize = 14.sp)
                                BanglaText(" / ${banglaNumerals(minT)}°", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                        if (i < count - 1) {
                            Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
fun WeatherDetailInfo(label: String, value: String, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BanglaText(label, color = textColor.copy(alpha = 0.7f), fontSize = 12.sp)
        BanglaHeading(value, color = textColor, fontSize = 16.sp)
    }
}

fun banglaNumerals(number: Int): String {
    val bengaliDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val str = number.toString()
    val sb = java.lang.StringBuilder()
    for (char in str) {
        if (char.isDigit()) {
            sb.append(bengaliDigits[char - '0'])
        } else {
            sb.append(char)
        }
    }
    return sb.toString()
}

fun formatHour(isoTime: String): String {
    try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
        val date = parser.parse(isoTime) ?: return ":"
        val formatter = SimpleDateFormat("hh a", Locale("bn", "BD"))
        return formatter.format(date)
    } catch (e: Exception) {
        return ":"
    }
}

fun formatDay(isoTime: String): String {
    try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = parser.parse(isoTime) ?: return "-"
        val formatter = SimpleDateFormat("EEEE", Locale("bn", "BD"))
        return formatter.format(date)
    } catch (e: Exception) {
        return "-"
    }
}

fun getCurrentTimeStr(): String {
    val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale("bn", "BD"))
    return formatter.format(Date())
}
