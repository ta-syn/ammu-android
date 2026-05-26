package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.material3.Surface
import com.example.ui.components.BanglaText
import com.example.ui.utils.NetworkMonitor
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.layout.AppHeader
import com.example.ui.layout.MoreBottomSheet
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.health.HealthScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.medicine.MedicineScreen
import com.example.ui.screens.hospital.HospitalScreen
import com.example.ui.screens.more.MoreScreen
import com.example.ui.screens.prayer.PrayerScreen
import com.example.ui.theme.GreenLight

enum class Screen(val route: String, val title: String, val icon: ImageVector?) {
    Login("login", "লগইন", null),
    Register("register", "অ্যাকাউন্ট খুলুন", null),
    Home("home", "হোম", Icons.Filled.Home),
    Prayer("prayer", "নামাজ", Icons.Filled.Star),
    Chat("chat", "AI চ্যাট", Icons.Filled.ChatBubble),
    Health("health", "স্বাস্থ্য", Icons.Filled.Favorite),
    Quran("quran", "কুরআন", Icons.Filled.Book), // Using arbitrary icon since not in bottom nav
    Tasbih("tasbih", "তাসবিহ", null),
    Qibla("qibla", "কিবলা", null),
    Dua("dua", "দোয়া", null),
    Medicine("medicine", "ওষুধ", null),
    Hospital("hospital", "হাসপাতাল", null),
    Recipe("recipe", "রেসিপি", null),
    News("news", "সংবাদ", null),
    Shopping("shopping", "বাজার", null),
    Family("family", "পরিবার", null),
    Journal("journal", "ডায়েরি", null),
    Settings("settings", "সেটিংস", null),
    Weather("weather", "আবহাওয়া", null),
    Expense("expense", "খরচ", null),
    Onboarding("onboarding", "স্বাগতম", null),
    More("more", "আরও", Icons.Filled.MoreHoriz);

    companion object {
        fun fromRoute(route: String?): Screen? {
            return values().find { route?.startsWith(it.route) == true }
        }
    }
}

val bottomNavigationItems = listOf(
    Screen.Home,
    Screen.Prayer,
    Screen.Chat,
    Screen.Health,
    Screen.More
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var showMoreSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val networkMonitor = remember { NetworkMonitor(context) }
    val isConnected by networkMonitor.isConnected.collectAsState()

    Scaffold(
        topBar = {
            androidx.compose.foundation.layout.Column {
                if (!isConnected) {
                    Surface(
                        color = Color(0xFFFFF3CD),
                        modifier = Modifier.fillMaxWidth().clickable { 
                            navController.navigate(Screen.Tasbih.route)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BanglaText(
                                "ইন্টারনেট সংযোগ নেই — তবুও নামাজের ওয়াক্ত ও হাদিস দেখুন",
                                color = Color(0xFF856404),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            BanglaText(
                                "তাসবিহ পড়ুন 📿",
                                color = GreenLight,
                                fontSize = 12.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }
                }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val isAuthScreen = currentDestination?.route == Screen.Login.route || currentDestination?.route == Screen.Register.route || currentDestination?.route == Screen.Onboarding.route
                
                if (!isAuthScreen) {
                    // Find current screen title
                    val currentScreen = Screen.fromRoute(currentDestination?.route)
                    if (currentDestination?.route?.startsWith("surah_reader") != true) {
                        AppHeader(title = currentScreen?.title ?: "আম্মু")
                    }
                }
            }
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val isAuthScreen = currentDestination?.route == Screen.Login.route || currentDestination?.route == Screen.Register.route || currentDestination?.route == Screen.Onboarding.route

            if (!isAuthScreen) {
                NavigationBar {
                    bottomNavigationItems.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        val isChat = screen == Screen.Chat
                        
                        NavigationBarItem(
                            icon = { 
                                screen.icon?.let { 
                                    if (isChat) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(GreenLight, CircleShape),
                                            contentAlignment = androidx.compose.ui.Alignment.Center
                                        ) {
                                            Icon(it, contentDescription = screen.title, tint = Color.White)
                                        }
                                    } else {
                                        Icon(it, contentDescription = screen.title) 
                                    }
                                } 
                            },
                            label = { 
                                if (!isChat || isSelected) {
                                    Text(screen.title) 
                                }
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = if(isChat) Color.Transparent else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            onClick = {
                                if (screen == Screen.More) {
                                    showMoreSheet = true
                                } else {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) { 
                com.example.ui.screens.auth.LoginScreen(
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onLoginSuccess = { 
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        } 
                    }
                ) 
            }
            composable(Screen.Register.route) { 
                com.example.ui.screens.auth.RegisterScreen(
                    onNavigateToLogin = { navController.navigateUp() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Onboarding.route) {
                com.example.ui.screens.onboarding.OnboardingScreen(
                    onFinish = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Prayer.route) { PrayerScreen() }
            composable(Screen.Chat.route) { ChatScreen() }
            composable(Screen.Health.route) { HealthScreen() }
            composable(Screen.More.route) { MoreScreen() }
            composable(Screen.Tasbih.route) { com.example.ui.screens.tasbih.TasbihScreen() }
            composable(Screen.Qibla.route) { com.example.ui.screens.qibla.QiblaScreen() }
            composable(Screen.Dua.route) { com.example.ui.screens.dua.DuaScreen() }
            composable(Screen.Medicine.route) { MedicineScreen() }
            composable(Screen.Hospital.route) { HospitalScreen() }
            composable(Screen.Recipe.route) { com.example.ui.screens.recipe.RecipeScreen() }
            composable(Screen.News.route) { com.example.ui.screens.news.NewsScreen() }
            composable(Screen.Shopping.route) { com.example.ui.screens.shopping.ShoppingScreen() }
            composable(Screen.Family.route) { com.example.ui.screens.family.FamilyScreen() }
            composable(Screen.Journal.route) { com.example.ui.screens.journal.JournalScreen() }
            composable(Screen.Settings.route) { com.example.ui.screens.settings.SettingsScreen() }
            composable(Screen.Weather.route) { com.example.ui.screens.weather.WeatherScreen() }
            composable(Screen.Expense.route) { com.example.ui.screens.expense.ExpenseScreen() }
        }

        
        if (showMoreSheet) {
            MoreBottomSheet(
                onDismissRequest = { showMoreSheet = false },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
    }
}
