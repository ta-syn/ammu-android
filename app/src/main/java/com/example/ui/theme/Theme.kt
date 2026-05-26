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
import androidx.compose.runtime.staticCompositionLocalOf
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

val LocalFontScale = staticCompositionLocalOf { 1.0f }
val LocalLargeButtonMode = staticCompositionLocalOf { false }

@Composable
fun MyApplicationTheme(
  theme: Int = 0,
  colorTheme: Int = 0,
  highContrast: Boolean = false,
  fontSize: Int = 1,
  largeButtonMode: Boolean = false,
  content: @Composable () -> Unit,
) {
  val darkTheme = when (theme) {
    0 -> isSystemInDarkTheme()
    1 -> false
    2 -> true
    else -> isSystemInDarkTheme()
  }

  val baseColorScheme = if (darkTheme) {
    darkColorScheme(
      primary = when (colorTheme) {
        1 -> Color(0xFF90CAF9)
        2 -> Color(0xFFE1BEE7)
        else -> GreenLight
      },
      onPrimary = Color(0xFF0A1F1C),
      primaryContainer = when (colorTheme) {
        1 -> Color(0xFF1565C0)
        2 -> Color(0xFF6A1B9A)
        else -> GreenDark
      },
      onPrimaryContainer = GoldAccent,
      secondary = when (colorTheme) {
        1 -> Color(0xFF90CAF9)
        2 -> Color(0xFFE1BEE7)
        else -> GreenLight
      },
      tertiary = GoldAccent,
      background = if (highContrast) Color.Black else SurfaceDark,
      surface = if (highContrast) Color.Black else SurfaceDark,
      onSecondary = Color(0xFF0A1F1C),
      onTertiary = TextLight,
      onBackground = TextDark,
      onSurface = TextDark,
      error = DangerSoft
    )
  } else {
    lightColorScheme(
      primary = when (colorTheme) {
        1 -> Color(0xFF1976D2)
        2 -> Color(0xFF9C27B0)
        else -> GreenPrimary
      },
      onPrimary = TextOnPrimary,
      primaryContainer = when (colorTheme) {
        1 -> Color(0xFFE3F2FD)
        2 -> Color(0xFFF3E5F5)
        else -> Color(0xFFE8F5F3)
      },
      onPrimaryContainer = when (colorTheme) {
        1 -> Color(0xFF0D47A1)
        2 -> Color(0xFF4A148C)
        else -> Color(0xFF012420)
      },
      secondary = when (colorTheme) {
        1 -> Color(0xFF1976D2)
        2 -> Color(0xFF9C27B0)
        else -> GreenPrimary
      },
      tertiary = GoldAccent,
      background = if (highContrast) Color.White else SurfaceLight,
      surface = if (highContrast) Color.White else SurfaceLight,
      onSecondary = TextOnPrimary,
      onTertiary = TextLight,
      onBackground = TextLight,
      onSurface = TextLight,
      error = DangerSoft
    )
  }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = baseColorScheme.background.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }
  }

  val fontScale = when (fontSize) {
    0 -> 0.85f
    1 -> 1.0f
    2 -> 1.15f
    3 -> 1.30f
    else -> 1.0f
  }

  androidx.compose.runtime.CompositionLocalProvider(
    LocalFontScale provides fontScale,
    LocalLargeButtonMode provides largeButtonMode
  ) {
    MaterialTheme(colorScheme = baseColorScheme, typography = Typography, content = content)
  }
}
