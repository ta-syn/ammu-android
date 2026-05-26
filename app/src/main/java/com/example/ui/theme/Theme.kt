package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
  darkColorScheme(
    primary = GreenLight,
    onPrimary = Color(0xFF0A1F1C),
    primaryContainer = GreenDark,
    onPrimaryContainer = GoldAccent,
    secondary = GreenLight,
    tertiary = GoldAccent,
    background = SurfaceDark,
    surface = SurfaceDark,
    onSecondary = Color(0xFF0A1F1C),
    onTertiary = TextLight,
    onBackground = TextDark,
    onSurface = TextDark,
    error = DangerSoft
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GreenPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = Color(0xFFE8F5F3),
    onPrimaryContainer = Color(0xFF012420),
    secondary = GreenPrimary,
    tertiary = GoldAccent,
    background = SurfaceLight,
    surface = SurfaceLight,
    onSecondary = TextOnPrimary,
    onTertiary = TextLight,
    onBackground = TextLight,
    onSurface = TextLight,
    error = DangerSoft
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled to force our Islamic Theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.background.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
