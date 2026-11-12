package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// --- Theme Palettes ---
enum class ThemePalette(val displayNameFa: String, val displayNameEn: String, val primaryColor: Color) {
    OCEAN_BLUE("آبی اقیانوسی", "Ocean Blue", Color(0xFF0052B4)),
    EMERALD_GREEN("سبز زمردی", "Emerald Green", Color(0xFF059669)),
    ROYAL_PURPLE("بنفش سلطنتی", "Royal Purple", Color(0xFF7C3AED)),
    AMBER_SUNSET("غروب کهربایی", "Amber Sunset", Color(0xFFD97706))
}

enum class ThemeMode(val displayNameFa: String, val displayNameEn: String) {
    SYSTEM("خودکار سیستم", "System Default"),
    LIGHT("روشن", "Light Mode"),
    DARK("تاریک", "Dark Mode")
}

// 1. Ocean Blue Palette (Default)
val BrandBlue = Color(0xFF0052B4)
val BrandBlueDark = Color(0xFF003780)
val BrandBlueLight = Color(0xFFDBEAFE)
val OnBrandBlue = Color(0xFFFFFFFF)
val OnBrandBlueLight = Color(0xFF0A2540)

// 2. Emerald Green Palette
val EmeraldPrimary = Color(0xFF059669)
val EmeraldDark = Color(0xFF065F46)
val EmeraldLight = Color(0xFFD1FAE5)
val OnEmeraldLight = Color(0xFF064E3B)

// 3. Royal Purple Palette
val PurplePrimary = Color(0xFF7C3AED)
val PurpleDark = Color(0xFF5B21B6)
val PurpleLight = Color(0xFFEDE9FE)
val OnPurpleLight = Color(0xFF3B0764)

// 4. Amber Sunset Palette
val AmberPrimary = Color(0xFFD97706)
val AmberDark = Color(0xFF92400E)
val AmberLight = Color(0xFFFEF3C7)
val OnAmberLight = Color(0xFF78350F)

// High-Contrast Ink Hierarchy (Light Mode)
val DeepInk = Color(0xFF0F172A)        // Primary titles & values
val SlateText = Color(0xFF334155)      // Secondary labels & metadata
val LightSlateText = Color(0xFF475569) // Supporting captions

// Surfaces & Borders (Light Mode)
val CleanWhiteBg = Color(0xFFF1F5F9)   // Cool slate canvas
val ElegantSurface = Color(0xFFFFFFFF) // Crisp white cards
val SubtleSurface = Color(0xFFF8FAFC)  // Inner metric containers
val CrispBorder = Color(0xFFCBD5E1)    // Card & container borders

// Dark Mode Tokens
val DarkCanvasBg = Color(0xFF090D16)   // Deep rich dark canvas
val DarkSurfaceCard = Color(0xFF131D31)// Elevated dark surface
val DarkSubtleSurface = Color(0xFF1E293B)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkBorder = Color(0xFF283548)

// Compatibility tokens
val GlassBorder = Color(0xFFCBD5E1)
val GlassBackground = Color(0xFFF1F5F9)
val GlassSurface = Color(0xFFFFFFFF)

val Purple80 = Color(0xFF0052B4)
val PurpleGrey80 = Color(0xFF334155)
val Pink80 = Color(0xFF0F172A)

val Purple40 = Color(0xFF0052B4)
val PurpleGrey40 = Color(0xFF334155)
val Pink40 = Color(0xFF0F172A)
