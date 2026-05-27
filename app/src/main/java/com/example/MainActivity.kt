package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.network.supabaseClient
import com.example.ui.utils.SystemNotificationHelper
import io.github.jan.supabase.auth.handleDeeplinks

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supabaseClient.handleDeeplinks(intent)
    SystemNotificationHelper.initNotificationChannel(this)
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }
    }
    
    enableEdgeToEdge()
    setContent {
      val settingsViewModel: com.example.ui.screens.settings.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
      val settingsState by settingsViewModel.state.collectAsState()

      MyApplicationTheme(
        theme = settingsState.theme,
        colorTheme = settingsState.colorTheme,
        highContrast = settingsState.highContrast,
        fontSize = settingsState.fontSize,
        largeButtonMode = settingsState.largeButtonMode
      ) {
        AppNavigation()
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    supabaseClient.handleDeeplinks(intent)
  }
}

