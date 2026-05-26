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

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                if (currentUser != null) {
                    val meta = currentUser.rawUserMetadata
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
            // Next Prayer Card
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

            // Today's Medicine Card
            MedicineCard(onClick = medicineNav)

            // Daily Dua Widget
            DailyDuaWidget()

            // Daily Quran Widget
            QuranWidget()

            // Health Summary Card
            HealthSummaryCard()

            // Today's Hadith Preview
            HadithPreview(onClick = { navController.navigate("hadith") })
            
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
                val (greeting, emoji) = when (java.time.LocalTime.now().hour) {
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
                        BanglaHeading(
                            text = "$greeting, $userName $emoji",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 22.sp
                        )
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
fun HadithPreview(onClick: () -> Unit = {}) {
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
