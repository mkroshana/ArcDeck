package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = StaticPrimaryNeon,
    secondary = StaticSecondaryTech,
    tertiary = StaticAccentPulse,
    background = Color(0xFF0A0A0C),
    surface = Color(0xFF0A0A0C),
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = Color(0x1100E5FF),
    secondaryContainer = Color(0x1194A3B8),
    surfaceContainer = Color(0xFF1E1E22),
    outlineVariant = StaticThemeCardBorder
  )

private val LightColorScheme = DarkColorScheme // Always use dark command center theme for this applet


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
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

  MaterialExpressiveTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
