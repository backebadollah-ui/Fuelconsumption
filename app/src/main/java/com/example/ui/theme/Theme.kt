package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HighContrastLightColorScheme =
  lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueLight,
    onPrimaryContainer = OnBrandBlueLight,
    secondary = SlateText,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = DeepInk,
    tertiary = Color(0xFF0D9488),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCFBF1),
    onTertiaryContainer = Color(0xFF134E4A),
    background = CleanWhiteBg,
    onBackground = DeepInk,
    surface = ElegantSurface,
    onSurface = DeepInk,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = SlateText,
    outline = Color(0xFF64748B),
    outlineVariant = CrispBorder,
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D)
  )

@Composable
fun MyApplicationTheme(
  // Keep consistent curated high-contrast light scheme so system dark mode or wallpaper
  // dynamic colors never turn text pale/white on top of white dashboard cards.
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = HighContrastLightColorScheme,
    typography = Typography,
    content = content
  )
}
