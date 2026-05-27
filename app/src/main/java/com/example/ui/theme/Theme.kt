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
    primary = GoldAccent,
    onPrimary = GreenPrimary,
    primaryContainer = GreenPrimary,
    onPrimaryContainer = GoldAccent,
    secondary = GoldAccent,
    tertiary = GoldAccent,
    background = SurfaceDark,
    surface = SurfaceDark,
    onSecondary = GreenPrimary,
    onTertiary = GreenPrimary,
    onBackground = GoldAccent,
    onSurface = GoldAccent,
    error = DangerSoft
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GoldAccent,
    primaryContainer = GoldAccent,
    onPrimaryContainer = GreenPrimary,
    secondary = GreenPrimary,
    tertiary = GoldAccent,
    background = SurfaceLight,
    surface = SurfaceLight,
    onSecondary = GoldAccent,
    onTertiary = GreenPrimary,
    onBackground = GreenPrimary,
    onSurface = GreenPrimary,
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
        else -> GoldAccent
      },
      onPrimary = when (colorTheme) {
        1 -> Color.Black
        2 -> Color.Black
        else -> GreenPrimary
      },
      primaryContainer = when (colorTheme) {
        1 -> Color(0xFF1565C0)
        2 -> Color(0xFF6A1B9A)
        else -> GreenPrimary
      },
      onPrimaryContainer = when (colorTheme) {
        1 -> Color.White
        2 -> Color.White
        else -> GoldAccent
      },
      secondary = when (colorTheme) {
        1 -> Color(0xFF90CAF9)
        2 -> Color(0xFFE1BEE7)
        else -> GoldAccent
      },
      onSecondary = when (colorTheme) {
        1 -> Color.Black
        2 -> Color.Black
        else -> GreenPrimary
      },
      secondaryContainer = when (colorTheme) {
        1 -> Color(0xFF1565C0)
        2 -> Color(0xFF6A1B9A)
        else -> GreenPrimary
      },
      onSecondaryContainer = when (colorTheme) {
        1 -> Color.White
        2 -> Color.White
        else -> GoldAccent
      },
      tertiary = GoldAccent,
      onTertiary = GreenPrimary,
      surfaceVariant = when (colorTheme) {
        1 -> Color(0xFF1565C0)
        2 -> Color(0xFF6A1B9A)
        else -> GreenPrimary
      },
      onSurfaceVariant = when (colorTheme) {
        1 -> Color.White
        2 -> Color.White
        else -> GoldAccent
      },
      background = if (highContrast) Color.Black else SurfaceDark,
      surface = if (highContrast) Color.Black else SurfaceDark,
      onBackground = when (colorTheme) {
        1 -> TextDark
        2 -> TextDark
        else -> GoldAccent
      },
      onSurface = when (colorTheme) {
        1 -> TextDark
        2 -> TextDark
        else -> GoldAccent
      },
      error = DangerSoft
    )
  } else {
    lightColorScheme(
      primary = when (colorTheme) {
        1 -> Color(0xFF1976D2)
        2 -> Color(0xFF9C27B0)
        else -> GreenPrimary
      },
      onPrimary = when (colorTheme) {
        1 -> Color.White
        2 -> Color.White
        else -> GoldAccent
      },
      primaryContainer = when (colorTheme) {
        1 -> Color(0xFFE3F2FD)
        2 -> Color(0xFFF3E5F5)
        else -> GoldAccent
      },
      onPrimaryContainer = when (colorTheme) {
        1 -> Color(0xFF0D47A1)
        2 -> Color(0xFF4A148C)
        else -> GreenPrimary
      },
      secondary = when (colorTheme) {
        1 -> Color(0xFF1976D2)
        2 -> Color(0xFF9C27B0)
        else -> GreenPrimary
      },
      onSecondary = when (colorTheme) {
        1 -> Color.White
        2 -> Color.White
        else -> GoldAccent
      },
      secondaryContainer = when (colorTheme) {
        1 -> Color(0xFFE3F2FD)
        2 -> Color(0xFFF3E5F5)
        else -> GoldAccent
      },
      onSecondaryContainer = when (colorTheme) {
        1 -> Color(0xFF0D47A1)
        2 -> Color(0xFF4A148C)
        else -> GreenPrimary
      },
      tertiary = GoldAccent,
      onTertiary = GreenPrimary,
      surfaceVariant = when (colorTheme) {
        1 -> Color(0xFFE3F2FD)
        2 -> Color(0xFFF3E5F5)
        else -> GoldAccent
      },
      onSurfaceVariant = when (colorTheme) {
        1 -> Color(0xFF0D47A1)
        2 -> Color(0xFF4A148C)
        else -> GreenPrimary
      },
      background = if (highContrast) Color.White else SurfaceLight,
      surface = if (highContrast) Color.White else SurfaceLight,
      onBackground = when (colorTheme) {
        1 -> TextLight
        2 -> TextLight
        else -> GreenPrimary
      },
      onSurface = when (colorTheme) {
        1 -> TextLight
        2 -> TextLight
        else -> GreenPrimary
      },
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
