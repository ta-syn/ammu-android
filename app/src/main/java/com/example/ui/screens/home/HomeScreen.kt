package com.example.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.example.ui.theme.GreenLight
import com.example.ui.theme.InfoCalm
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

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
import com.example.ui.components.NotificationBell
import com.example.ui.components.NotificationSheet
import com.example.ui.components.NotificationSharedViewModel
import com.example.ui.screens.weather.WeatherViewModel
import com.example.ui.screens.weather.WeatherUiState

@Composable
fun HomeScreen(navController: NavController = rememberNavController()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        val weatherNav = { navController.navigate("weather") }
        // Hero Section
        HeroSection(onWeatherClick = weatherNav)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Next Prayer Card
            NextPrayerCard()

            val quranNav = { navController.navigate("quran") }
            val tasbihNav = { navController.navigate("tasbih") }
            val qiblaNav = { navController.navigate("qibla") }
            val duaNav = { navController.navigate("dua") }
            val medicineNav = { navController.navigate("medicine") }
            val hospitalNav = { navController.navigate("hospital") }
            val recipeNav = { navController.navigate("recipe") }
            val newsNav = { navController.navigate("news") }
            val shoppingNav = { navController.navigate("shopping") }
            val familyNav = { navController.navigate("family") }
            val journalNav = { navController.navigate("journal") }
            val settingsNav = { navController.navigate("settings") }
            val expenseNav = { navController.navigate("expense") }
            // Quick Actions Row
            QuickActionsRow(
                onNavigateToQuran = quranNav,
                onNavigateToTasbih = tasbihNav,
                onNavigateToQibla = qiblaNav,
                onNavigateToDua = duaNav,
                onNavigateToMedicine = medicineNav,
                onNavigateToHospital = hospitalNav,
                onNavigateToRecipe = recipeNav,
                onNavigateToNews = newsNav,
                onNavigateToShopping = shoppingNav,
                onNavigateToFamily = familyNav,
                onNavigateToJournal = journalNav,
                onNavigateToSettings = settingsNav,
                onNavigateToExpense = expenseNav
            )

            // Today's Medicine Card
            MedicineCard(onClick = medicineNav)

            // Daily Dua Widget
            DailyDuaWidget()

            // Daily Quran Widget
            QuranWidget()

            // Health Summary Card
            HealthSummaryCard()

            // Today's Hadith Preview
            HadithPreview()
            
            // Family Activity Card
            FamilyActivityCard()
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun HeroSection(
    notificationViewModel: NotificationSharedViewModel = viewModel(),
    weatherViewModel: WeatherViewModel = viewModel(),
    onWeatherClick: () -> Unit = {}
) {
    val notifications by notificationViewModel.notifications.collectAsState()
    val weatherState by weatherViewModel.uiState.collectAsState()
    
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
                .padding(top = 24.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Column {
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    visible = true
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = visible,
                        enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(1500)) +
                                androidx.compose.animation.slideInVertically(initialOffsetY = { 20 })
                    ) {
                        BanglaHeading(text = "শুভ সকাল ☀️ আম্মু", color = MaterialTheme.colorScheme.primary)
                    }
                    Row {
                        // Test Button to simulate incoming push notification
                        IconButton(onClick = {
                            val newNotif = com.example.data.local.entity.AppNotification(
                                title = "🕌 আসরের নামাজের সময়",
                                message = "আসরের নামাজের সময় শুরু হয়েছে।",
                                type = "prayer"
                            )
                            notificationViewModel.simulateNewNotification(newNotif.title, newNotif.message, newNotif.type)
                            latestPushNotif = newNotif
                        }) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Test Notification")
                        }
                        NotificationBell(
                            notifications = notifications,
                            onNotificationClick = { showNotifs = true }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                
                var weatherText = "১৪ জিলহজ, ১৪৪৬ | আবহাওয়া তথ্য লোড হচ্ছে..."
                if (weatherState is WeatherUiState.Success) {
                    val weather = (weatherState as WeatherUiState.Success).weather
                    val current = weather.current_weather
                    val (emoji, desc) = weatherViewModel.getWeatherEmojiAndDesc(current?.weathercode ?: 0)
                    weatherText = "১৪ জিলহজ | $emoji $desc — তাপমাত্রা ${com.example.ui.screens.weather.banglaNumerals(current?.temperature?.toInt() ?: 0)}°C"
                }

                BanglaText(
                    text = weatherText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                BanglaText(
                    text = "\"নিশ্চয়ই আল্লাহ ধৈর্যশীলদের সাথে আছেন।\" (সূরা বাকারা: ১৫৩)",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )
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
fun NextPrayerCard() {
    Column {
        CardIslamic(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BanglaHeading(
                        text = "পরবর্তী নামাজ: আসর",
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .background(GreenLight.copy(alpha = 0.2f), RoundedCornerShape(Radius.sm))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        BanglaText(text = "৩/৫ ✓", color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
                BanglaText(text = "১ ঘণ্টা ২৩ মিনিট বাকি", modifier = Modifier.padding(bottom = 8.dp))
                
                LinearProgressIndicator(
                    progress = 0.6f,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(Radius.full)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        val prayers = listOf("ফজর" to "৪:৩০", "যোহর" to "১:১৫", "আসর" to "৪:৪৫", "মাগরিব" to "৬:৩০", "এশা" to "৮:০০")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(prayers) { prayer ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(Radius.sm),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BanglaText(text = prayer.first, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        BanglaText(text = prayer.second, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

data class QuickAction(val id: String, val title: String, val emoji: String)

@Composable
fun QuickActionsRow(
    onNavigateToQuran: () -> Unit = {},
    onNavigateToTasbih: () -> Unit = {},
    onNavigateToQibla: () -> Unit = {},
    onNavigateToDua: () -> Unit = {},
    onNavigateToMedicine: () -> Unit = {},
    onNavigateToHospital: () -> Unit = {},
    onNavigateToRecipe: () -> Unit = {},
    onNavigateToNews: () -> Unit = {},
    onNavigateToShopping: () -> Unit = {},
    onNavigateToFamily: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToExpense: () -> Unit = {}
) {
    val actions = listOf(
        QuickAction("chat", "AI চ্যাট", "💬"),
        QuickAction("expense", "খরচ", "💰"),
        QuickAction("quran", "কোরআন", "📖"),
        QuickAction("hospital", "হাসপাতাল", "🏥"),
        QuickAction("medicine", "ওষুধ", "💊"),
        QuickAction("family", "পরিবার", "👨‍👩‍👧‍👦"),
        QuickAction("shopping", "বাজার", "🛒"),
        QuickAction("news", "সংবাদ", "📰"),
        QuickAction("recipe", "রেসিপি", "🍳"),
        QuickAction("journal", "ডায়েরি", "📓"),
        QuickAction("settings", "সেটিংস", "⚙️")
    )

    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
    ) {
        items(actions) { action ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    if (action.id == "quran") {
                        onNavigateToQuran()
                    } else if (action.id == "tasbih") {
                        onNavigateToTasbih()
                    } else if (action.id == "qibla") {
                        onNavigateToQibla()
                    } else if (action.id == "dua") {
                        onNavigateToDua()
                    } else if (action.id == "medicine") {
                        onNavigateToMedicine()
                    } else if (action.id == "hospital") {
                        onNavigateToHospital()
                    } else if (action.id == "recipe") {
                        onNavigateToRecipe()
                    } else if (action.id == "news") {
                        onNavigateToNews()
                    } else if (action.id == "shopping") {
                        onNavigateToShopping()
                    } else if (action.id == "family") {
                        onNavigateToFamily()
                    } else if (action.id == "journal") {
                        onNavigateToJournal()
                    } else if (action.id == "settings") {
                        onNavigateToSettings()
                    } else if (action.id == "expense") {
                        onNavigateToExpense()
                    }
                }
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Text(
                            text = action.emoji,
                            fontSize = 28.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                BanglaText(text = action.title, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun MedicineCard(onClick: () -> Unit = {}) {
    CardBase(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                BanglaHeading(text = "সকালের ওষুধ 💊")
                Spacer(modifier = Modifier.height(4.dp))
                BanglaText(text = "মেটফর্মিন — ৮:০০ টায়", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                color = GreenLight.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.padding(8.dp)
            ) {
                BanglaText(
                    text = "খেয়েছি ✓",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun QuranWidget() {
    CardIslamic(modifier = Modifier.fillMaxWidth()) {
        Column {
            androidx.compose.material3.Text(
                text = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ",
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
fun HealthSummaryCard() {
    CardHealth(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BanglaHeading(text = "স্বাস্থ্য 💚", color = InfoCalm)
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = InfoCalm)
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            BanglaText(text = "শেষ রক্তচাপ: ১২০/৮০ mmHg ↓ (স্বাভাবিক)")
        }
    }
}

@Composable
fun HadithPreview() {
    CardBase(modifier = Modifier.fillMaxWidth()) {
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
                    text = "\"মায়ের পায়ের নিচে সন্তানের বেহেশত।\"",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                BanglaText(text = "বিস্তারিত পড়ুন →", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun FamilyActivityCard() {
    CardBase(modifier = Modifier.fillMaxWidth()) {
        Column {
            BanglaHeading(text = "আপনার পরিবার 👨‍👩‍👧‍👦")
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.weight(1f)) {
                    repeat(3) {
                        Surface(
                            modifier = Modifier.size(40.dp).padding(end = 4.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                BanglaText(text = "সবাই ভালো আছে 💚", color = GreenLight, fontWeight = FontWeight.Bold)
            }
        }
    }
}
