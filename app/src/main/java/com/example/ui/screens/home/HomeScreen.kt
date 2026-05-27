package com.example.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.components.CardBase
import com.example.ui.components.CardHealth
import com.example.ui.components.CardIslamic
import com.example.ui.components.Radius
import com.example.ui.components.Spacing
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InfoCalm
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.zIndex
import androidx.compose.material3.IconButton
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.dua.DailyDuaWidget
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.jsonPrimitive
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.example.network.supabaseClient
import com.example.data.local.DatabaseProvider
import com.example.data.local.entity.Profile
import com.example.ui.components.NotificationBell
import com.example.ui.components.NotificationSheet
import com.example.ui.components.NotificationSharedViewModel
import com.example.ui.screens.weather.WeatherViewModel
import com.example.ui.screens.weather.WeatherUiState

@Composable
fun HomeScreen(navController: NavController = rememberNavController()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dailyReminder by remember { mutableStateOf("মায়ের পায়ের নিচে সন্তানের বেহেশত।") }

    LaunchedEffect(Unit) {
        // Fetch daily content from Supabase
        scope.launch(Dispatchers.IO) {
            try {
                val response = supabaseClient.postgrest.from("daily_content")
                    .select {
                        filter {
                            eq("content_type", "reminder")
                        }
                        order(column = "created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                        limit(1)
                    }
                val jsonArray = org.json.JSONArray(response.data)
                if (jsonArray.length() > 0) {
                    val first = jsonArray.getJSONObject(0)
                    val content = first.getString("content")
                    if (content.isNotBlank()) {
                        dailyReminder = content
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        scope.launch(Dispatchers.IO) {
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                if (currentUser != null) {
                    val meta = currentUser.userMetadata
                    val name = meta?.get("full_name")?.jsonPrimitive?.content
                        ?: meta?.get("name")?.jsonPrimitive?.content
                        ?: currentUser.email?.substringBefore("@")
                        ?: "ব্যবহারকারী"
                    val avatar = meta?.get("avatar_url")?.jsonPrimitive?.content
                        ?: meta?.get("picture")?.jsonPrimitive?.content

                    val dao = DatabaseProvider.getDatabase(context).ammuDao()
                    val existingProfile = dao.getProfile().firstOrNull()

                    val updatedProfile = Profile(
                        id = currentUser.id,
                        fullName = name,
                        avatarUrl = avatar,
                        phone = existingProfile?.phone ?: "",
                        dateOfBirth = existingProfile?.dateOfBirth ?: "",
                        locationCity = existingProfile?.locationCity ?: "Dhaka",
                        locationLat = existingProfile?.locationLat,
                        locationLng = existingProfile?.locationLng,
                        preferredFontSize = existingProfile?.preferredFontSize ?: "large",
                        subscriptionTier = existingProfile?.subscriptionTier ?: "free",
                        notificationEnabled = existingProfile?.notificationEnabled ?: true,
                        darkModePreference = existingProfile?.darkModePreference ?: "system"
                    )
                    dao.insertProfile(updatedProfile)

                    // Sync to Supabase profiles table
                    try {
                        val profileJson = buildJsonObject {
                            put("id", currentUser.id)
                            put("full_name", name)
                            put("notification_enabled", updatedProfile.notificationEnabled)
                        }
                        supabaseClient.postgrest.from("profiles").upsert(profileJson)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        val weatherNav = { navController.navigate("weather") }
        val settingsNav = { navController.navigate("settings") }
        // Hero Section
        HeroSection(
            onWeatherClick = weatherNav,
            onProfileClick = settingsNav
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Next Prayer Card (Dynamic)
            NextPrayerCard()

            val chatNav = { navController.navigate("chat") }
            val quranNav = { navController.navigate("quran") }
            val hadithNav = { navController.navigate("hadith") }
            val duaNav = { navController.navigate("dua") }
            val tasbihNav = { navController.navigate("tasbih") }
            val qiblaNav = { navController.navigate("qibla") }
            val medicineNav = { navController.navigate("medicine") }
            val hospitalNav = { navController.navigate("hospital") }
            val expenseNav = { navController.navigate("expense") }
            val shoppingNav = { navController.navigate("shopping") }
            val recipeNav = { navController.navigate("recipe") }
            val newsNav = { navController.navigate("news") }
            val journalNav = { navController.navigate("journal") }
            val familyNav = { navController.navigate("family") }
            val healthNav = { navController.navigate("health") }

            // Quick Actions Grid
            QuickActionsGrid(
                onNavigateToChat = chatNav,
                onNavigateToQuran = quranNav,
                onNavigateToHadith = hadithNav,
                onNavigateToDua = duaNav,
                onNavigateToTasbih = tasbihNav,
                onNavigateToQibla = qiblaNav,
                onNavigateToMedicine = medicineNav,
                onNavigateToHospital = hospitalNav,
                onNavigateToExpense = expenseNav,
                onNavigateToShopping = shoppingNav,
                onNavigateToRecipe = recipeNav,
                onNavigateToNews = newsNav,
                onNavigateToJournal = journalNav,
                onNavigateToFamily = familyNav,
                onNavigateToSettings = settingsNav
            )

            // Today's Medicine Card (Dynamic)
            MedicineCard(onClick = medicineNav)

            // Daily Dua Widget
            DailyDuaWidget()

            // Daily Quran Widget
            QuranWidget()

            // Health Summary Card (Dynamic)
            HealthSummaryCard(onClick = healthNav)

            // Today's Hadith Preview
            HadithPreview(text = dailyReminder, onClick = hadithNav)
            
            // Family Activity Card (Dynamic)
            FamilyActivityCard(onClick = familyNav)
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun HeroSection(
    notificationViewModel: NotificationSharedViewModel = viewModel(),
    weatherViewModel: WeatherViewModel = viewModel(),
    onWeatherClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val notifications by notificationViewModel.notifications.collectAsState()
    val weatherState by weatherViewModel.uiState.collectAsState()
    
    val context = LocalContext.current
    val db = remember { com.example.data.local.DatabaseProvider.getDatabase(context) }
    val profile by db.ammuDao().getProfile().collectAsState(initial = null)
    
    var showNotifs by remember { mutableStateOf(false) }
    var latestPushNotif by remember { mutableStateOf<com.example.data.local.entity.AppNotification?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(bottomStart = Radius.xl, bottomEnd = Radius.xl)
                )
                .clickable { onWeatherClick() }
                .statusBarsPadding()
                .padding(top = 16.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            Column {
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    visible = true
                }
                
                val userName = profile?.fullName ?: "আম্মু"
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val (greeting, emoji) = when (hour) {
                    in 5..11 -> "শুভ সকাল" to "☀️"
                    in 12..16 -> "শুভ দুপুর" to "☀️"
                    in 17..19 -> "শুভ সন্ধ্যা" to "🌅"
                    else -> "শুভ রাত্রি" to "🌙"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = visible,
                        enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(1500)) +
                                androidx.compose.animation.slideInVertically(initialOffsetY = { 20 })
                    ) {
                        Box(modifier = Modifier.clickable { onProfileClick() }) {
                            BanglaHeading(
                                text = "$greeting, $userName $emoji",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 22.sp
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Test Button to simulate incoming push notification
                        IconButton(
                            onClick = {
                                val newNotif = com.example.data.local.entity.AppNotification(
                                    title = "🕌 আসরের নামাজের সময়",
                                    message = "আসরের নামাজের সময় শুরু হয়েছে।",
                                    type = "prayer"
                                )
                                notificationViewModel.simulateNewNotification(newNotif.title, newNotif.message, newNotif.type)
                                latestPushNotif = newNotif
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Test Notification",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        }
                        NotificationBell(
                            notifications = notifications,
                            onNotificationClick = { showNotifs = true }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { onProfileClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profile?.avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = profile!!.avatarUrl,
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = "Profile",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                
                var weatherText = "১৪ জিলহজ, ১৪৪৬ | আবহাওয়া তথ্য লোড হচ্ছে..."
                if (weatherState is WeatherUiState.Success) {
                    val weather = (weatherState as WeatherUiState.Success).weather
                    val current = weather.current_weather
                    val (wEmoji, desc) = weatherViewModel.getWeatherEmojiAndDesc(current?.weathercode ?: 0)
                    weatherText = "১৪ জিলহজ | $wEmoji $desc — তাপমাত্রা ${com.example.ui.screens.weather.banglaNumerals(current?.temperature?.toInt() ?: 0)}°C"
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BanglaText(
                            text = "📍 $weatherText",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BanglaText(
                            text = "✨",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        BanglaText(
                            text = "\"নিশ্চয়ই আল্লাহ ধৈর্যশীলদের সাথে আছেন।\" (সূরা বাকারা: ১৫৩)",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        // Show banner layered on top
        if (latestPushNotif != null) {
            Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp).zIndex(10f)) {
                com.example.ui.components.InAppNotificationBanner(
                    notification = latestPushNotif,
                    onDismiss = { latestPushNotif = null },
                    onClick = {
                        latestPushNotif = null
                        showNotifs = true
                    }
                )
            }
        }
    }

    if (showNotifs) {
        NotificationSheet(
            notifications = notifications,
            onDismiss = { showNotifs = false },
            onMarkAsRead = { notificationViewModel.markAsRead(it) },
            onMarkAllAsRead = { notificationViewModel.markAllAsRead() },
            onClearAll = { notificationViewModel.clearAll() }
        )
    }
}

@Composable
fun NextPrayerCard(viewModel: com.example.ui.screens.prayer.PrayerViewModel = viewModel()) {
    val todayTimes by viewModel.todayPrayerTimes.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()

    val prayerKeys = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    val prayerNamesBn = mapOf("fajr" to "ফজর", "dhuhr" to "যোহর", "asr" to "আসর", "maghrib" to "মাগরিব", "isha" to "এশা")

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

    val prayedCount = todayLogs.count { it.status == "prayed" }
    val progress = if (prayedCount > 0) prayedCount.toFloat() / 5f else 0f

    Column {
        CardIslamic(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BanglaHeading(
                        text = "পরবর্তী নামাজ: $nextPrayerBn",
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(Radius.sm))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val countBn = toBengaliDigits(prayedCount.toString())
                        BanglaText(text = "$countBn/৫ ✓", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
                BanglaText(text = countdownText, modifier = Modifier.padding(bottom = 8.dp))
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(Radius.full)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(prayerKeys) { key ->
                val timeStr = todayTimes[key] ?: "12:00 PM"
                val nameBn = prayerNamesBn[key] ?: key
                val displayTime = toBengaliTime(timeStr)
                val isDone = todayLogs.any { it.prayerName == key && it.status == "prayed" }
                val isNext = key == nextPrayerKey

                Surface(
                    color = if (isDone) MaterialTheme.colorScheme.primaryContainer else if (isNext) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(Radius.sm),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BanglaText(
                            text = nameBn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isDone) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BanglaText(
                            text = displayTime,
                            color = if (isDone) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

data class QuickActionItem(
    val id: String,
    val title: String,
    val emoji: String,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun QuickActionsGrid(
    onNavigateToChat: () -> Unit = {},
    onNavigateToQuran: () -> Unit = {},
    onNavigateToHadith: () -> Unit = {},
    onNavigateToDua: () -> Unit = {},
    onNavigateToTasbih: () -> Unit = {},
    onNavigateToQibla: () -> Unit = {},
    onNavigateToMedicine: () -> Unit = {},
    onNavigateToHospital: () -> Unit = {},
    onNavigateToExpense: () -> Unit = {},
    onNavigateToShopping: () -> Unit = {},
    onNavigateToRecipe: () -> Unit = {},
    onNavigateToNews: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToFamily: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val items = listOf(
        QuickActionItem("chat", "AI চ্যাট", "💬", Color(0xFFEDE7F6), onNavigateToChat),
        QuickActionItem("quran", "কোরআন", "📖", Color(0xFFE8F5E9), onNavigateToQuran),
        QuickActionItem("hadith", "হাদিস", "📜", Color(0xFFE8F5E9), onNavigateToHadith),
        QuickActionItem("dua", "দোয়া", "🤲", Color(0xFFE8F5E9), onNavigateToDua),
        QuickActionItem("tasbih", "তাসবিহ", "📿", Color(0xFFE8F5E9), onNavigateToTasbih),
        QuickActionItem("qibla", "কিবলা", "🧭", Color(0xFFE8F5E9), onNavigateToQibla),
        QuickActionItem("medicine", "ওষুধ", "💊", Color(0xFFE3F2FD), onNavigateToMedicine),
        QuickActionItem("hospital", "হাসপাতাল", "🏥", Color(0xFFE3F2FD), onNavigateToHospital),
        QuickActionItem("expense", "খরচ", "💰", Color(0xFFFFF8E1), onNavigateToExpense),
        QuickActionItem("shopping", "বাজার", "🛒", Color(0xFFFFF8E1), onNavigateToShopping),
        QuickActionItem("recipe", "রেসিপি", "🍳", Color(0xFFFBE9E7), onNavigateToRecipe),
        QuickActionItem("news", "সংবাদ", "📰", Color(0xFFE0F2F1), onNavigateToNews),
        QuickActionItem("journal", "ডায়েরি", "📓", Color(0xFFEFEBE9), onNavigateToJournal),
        QuickActionItem("family", "পরিবার", "👨‍👩‍👧‍👦", Color(0xFFFCE4EC), onNavigateToFamily),
        QuickActionItem("settings", "সেটিংস", "⚙️", Color(0xFFECEFF1), onNavigateToSettings)
    )

    CardBase(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            BanglaHeading(
                text = "প্রধান সেবাসমূহ",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
            )
            
            val chunkSize = 4
            val rows = items.chunked(chunkSize)
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                for (rowItems in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (item in rowItems) {
                            GridItemCard(item = item, modifier = Modifier.weight(1f))
                        }
                        if (rowItems.size < chunkSize) {
                            repeat(chunkSize - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridItemCard(item: QuickActionItem, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = item.onClick)
            .padding(vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(12.dp),
            color = item.color,
            shadowElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                androidx.compose.material3.Text(
                    text = item.emoji,
                    fontSize = 24.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        BanglaText(
            text = item.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
fun MedicineCard(
    onClick: () -> Unit = {},
    viewModel: com.example.ui.screens.medicine.MedicineViewModel = viewModel()
) {
    val activeMeds by viewModel.activeMedicines.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()

    // Find the next upcoming log
    val nextLog = recentLogs
        .filter { it.status == "upcoming" }
        .minByOrNull { it.scheduledAt }

    val nextMed = nextLog?.let { log ->
        activeMeds.find { it.id == log.medicineId }
    }

    CardBase(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (nextMed != null && nextLog != null) {
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = nextLog.scheduledAt }
                    val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                    val minute = cal.get(java.util.Calendar.MINUTE)
                    val ampm = if (cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "সকাল" else "বিকাল/রাত"
                    val minStr = String.format("%02d", minute)
                    val timeStr = toBengaliDigits("$hour:$minStr")
                    
                    val medName = nextMed.banglaName ?: nextMed.medicineName
                    
                    BanglaHeading(text = "পরবর্তী ওষুধ 💊")
                    Spacer(modifier = Modifier.height(4.dp))
                    BanglaText(
                        text = "$medName — $ampm $timeStr টায় (${nextMed.dosage})",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    BanglaHeading(text = "ওষুধের তালিকা 💊")
                    Spacer(modifier = Modifier.height(4.dp))
                    BanglaText(
                        text = if (activeMeds.isEmpty()) "কোনো ওষুধ যোগ করা হয়নি" else "আজকের সব ওষুধ খাওয়া হয়েছে",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (nextMed != null && nextLog != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { viewModel.markLogAsTaken(nextLog) }
                ) {
                    BanglaText(
                        text = "খেয়েছি ✓",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                    modifier = Modifier.padding(8.dp)
                ) {
                    BanglaText(
                        text = "বিস্তারিত",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuranWidget() {
    CardIslamic(modifier = Modifier.fillMaxWidth()) {
        Column {
            androidx.compose.material3.Text(
                text = "👑 ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 40.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            BanglaText(text = "যাবতীয় প্রশংসা আল্লাহ তা'আলার যিনি সকল সৃষ্টি জগতের পালনকর্তা।")
            Spacer(modifier = Modifier.height(Spacing.sm))
            BanglaText(
                text = "সূরা আল ফাতিহা, আয়াত ২",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(Radius.full)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    BanglaText(text = "তেলাওয়াত শুনুন 🔊", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun HealthSummaryCard(
    onClick: () -> Unit = {},
    viewModel: com.example.ui.screens.health.HealthViewModel = viewModel()
) {
    val records by viewModel.records.collectAsState()
    val latestBp = records.filter { it.recordType == "bp" }.maxByOrNull { it.recordedAt }
    val latestSugar = records.filter { it.recordType == "sugar" }.maxByOrNull { it.recordedAt }
    
    CardHealth(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BanglaHeading(text = "স্বাস্থ্য 💚", color = InfoCalm)
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = InfoCalm)
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            if (latestBp != null) {
                val sys = latestBp.valuePrimary.toInt()
                val dia = latestBp.valueSecondary?.toInt() ?: 80
                val status = getBpStatusBn(latestBp.valuePrimary, latestBp.valueSecondary ?: 80.0)
                val sysBn = toBengaliDigits(sys.toString())
                val diaBn = toBengaliDigits(dia.toString())
                
                BanglaText(text = "শেষ রক্তচাপ: $sysBn/$diaBn mmHg ($status)")
            } else if (latestSugar != null) {
                val sugar = latestSugar.valuePrimary
                val sugarBn = toBengaliDigits(String.format("%.1f", sugar))
                BanglaText(text = "শেষ সুগার: $sugarBn mmol/L")
            } else {
                BanglaText(text = "কোনো স্বাস্থ্য রেকর্ড যুক্ত করা হয়নি। নতুন রেকর্ড যোগ করতে চাপ দিন।")
            }
        }
    }
}

@Composable
fun HadithPreview(text: String, onClick: () -> Unit = {}) {
    CardBase(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(Radius.sm))
            )
            Spacer(modifier = Modifier.width(Spacing.md))
            Column {
                BanglaText(
                    text = "\"$text\"",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                BanglaText(text = "বিস্তারিত পড়ুন →", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun FamilyActivityCard(
    onClick: () -> Unit = {},
    viewModel: com.example.ui.screens.family.FamilyViewModel = viewModel()
) {
    val members by viewModel.familyMembers.collectAsState()
    
    CardBase(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column {
            BanglaHeading(text = "আপনার পরিবার 👨‍👩‍👧‍👦")
            Spacer(modifier = Modifier.height(12.dp))
            
            if (members.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy((-8).dp) // overlapping style for premium feel
                    ) {
                        members.take(4).forEach { member ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(member.avatarColor), CircleShape)
                                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.name.take(1),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    val countBn = toBengaliDigits(members.size.toString())
                    BanglaText(
                        text = "পরিবারে $countBn জন সদস্য আছেন 💚",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                BanglaText(
                    text = "পরিবারের কোনো সদস্য যোগ করা হয়নি। যোগ করতে এখানে চাপুন।",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ---------------- Helpers ---------------- //

private fun parseTimeToMillis(timeStr: String): Long {
    return try {
        val cal = java.util.Calendar.getInstance()
        val cleanStr = timeStr.replace(" AM", "").replace(" PM", "").trim()
        val parts = cleanStr.split(":")
        var hour = parts[0].toInt()
        val min = parts[1].toInt()
        
        if (timeStr.contains("PM") && hour != 12) {
            hour += 12
        } else if (timeStr.contains("AM") && hour == 12) {
            hour = 0
        }
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
        cal.set(java.util.Calendar.MINUTE, min)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
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

private fun getBpStatusBn(systolic: Double, diastolic: Double): String {
    return if (systolic < 120 && diastolic < 80) {
        "স্বাভাবিক"
    } else if (systolic in 120.0..129.9 || diastolic in 80.0..84.9) {
        "সামান্য বেশি"
    } else {
        "উচ্চ রক্তচাপ"
    }
}
