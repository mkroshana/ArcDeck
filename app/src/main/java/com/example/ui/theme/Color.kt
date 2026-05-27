package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Static colors for fallback/default theme
val StaticThemeBackground = Color(0xFF121212) // Clean material dark background
val StaticThemeCardFill = Color(0xFF1E1E1E)   // Standard surface color
val StaticThemeCardBorder = Color(0xFF2C2C2C) // Subtle surface border
val StaticPrimaryNeon = Color(0xFF1976D2)    // Standard Material Blue
val StaticSecondaryTech = Color(0xFFB0BEC5)  // Standard Blue-Grey
val StaticAccentPulse = Color(0xFF5E35B1)    // Standard Deep Purple

val ThemeGridGlow = Color.Transparent   // Disabled glow

// Dynamic colors bound to the MaterialTheme
val ThemeBackground: Color
  @Composable
  get() = MaterialTheme.colorScheme.background

val ThemeCardFill: Color
  @Composable
  get() = MaterialTheme.colorScheme.surfaceContainer

val ThemeCardBorder: Color
  @Composable
  get() = MaterialTheme.colorScheme.outlineVariant

val PrimaryNeon: Color
  @Composable
  get() = MaterialTheme.colorScheme.primary

val SecondaryTech: Color
  @Composable
  get() = MaterialTheme.colorScheme.secondary

val AccentPulse: Color
  @Composable
  get() = MaterialTheme.colorScheme.tertiary

// Status Indicative Colors
val TechOk = Color(0xFF4CAF50)         // Standard Green
val TechWarning = Color(0xFFFFB300)    // Standard Amber
val TechCritical = Color(0xFFE53935)   // Standard Red
val TechMuted = Color(0xFF757575)      // Standard Grey

