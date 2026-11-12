package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun getLightColorScheme(palette: ThemePalette): ColorScheme {
    val (primary, primaryContainer, onPrimaryContainer) = when (palette) {
        ThemePalette.OCEAN_BLUE -> Triple(BrandBlue, BrandBlueLight, OnBrandBlueLight)
        ThemePalette.EMERALD_GREEN -> Triple(EmeraldPrimary, EmeraldLight, OnEmeraldLight)
        ThemePalette.ROYAL_PURPLE -> Triple(PurplePrimary, PurpleLight, OnPurpleLight)
        ThemePalette.AMBER_SUNSET -> Triple(AmberPrimary, AmberLight, OnAmberLight)
    }

    return lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
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
}

fun getDarkColorScheme(palette: ThemePalette): ColorScheme {
    val (primary, primaryContainer, onPrimaryContainer) = when (palette) {
        ThemePalette.OCEAN_BLUE -> Triple(Color(0xFF3B82F6), Color(0xFF1E3A8A), Color(0xFFDBEAFE))
        ThemePalette.EMERALD_GREEN -> Triple(Color(0xFF10B981), Color(0xFF064E3B), Color(0xFFD1FAE5))
        ThemePalette.ROYAL_PURPLE -> Triple(Color(0xFFA855F7), Color(0xFF581C87), Color(0xFFEDE9FE))
        ThemePalette.AMBER_SUNSET -> Triple(Color(0xFFF59E0B), Color(0xFF78350F), Color(0xFFFEF3C7))
    }

    return darkColorScheme(
        primary = primary,
        onPrimary = Color(0xFF090D16),
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = Color(0xFFCBD5E1),
        onSecondary = Color(0xFF090D16),
        secondaryContainer = Color(0xFF1E293B),
        onSecondaryContainer = Color(0xFFE2E8F0),
        tertiary = Color(0xFF2DD4BF),
        onTertiary = Color(0xFF090D16),
        tertiaryContainer = Color(0xFF134E4A),
        onTertiaryContainer = Color(0xFFCCFBF1),
        background = DarkCanvasBg,
        onBackground = DarkTextPrimary,
        surface = DarkSurfaceCard,
        onSurface = DarkTextPrimary,
        surfaceVariant = DarkSubtleSurface,
        onSurfaceVariant = DarkTextSecondary,
        outline = Color(0xFF64748B),
        outlineVariant = DarkBorder,
        error = Color(0xFFEF4444),
        onError = Color.White,
        errorContainer = Color(0xFF7F1D1D),
        onErrorContainer = Color(0xFFFEE2E2)
    )
}

@Composable
fun MyApplicationTheme(
    palette: ThemePalette = ThemePalette.OCEAN_BLUE,
    mode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit,
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (mode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) getDarkColorScheme(palette) else getLightColorScheme(palette)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
