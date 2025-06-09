package com.example.todolistcomposed.ui.theme // Ensure this matches your package

import androidx.compose.ui.graphics.Color

// Define your core dark theme colors
// For OLED, Black is your best friend for backgrounds/surfaces

val DarkPrimary = Color(0xFFBB86FC)       // A vibrant accent, e.g., for buttons, highlights
val DarkOnPrimary = Color(0xFF000000)     // Text/icons on Primary color

val DarkPrimaryContainer = Color(0xFF3700B3) // Slightly lighter than primary, for containers
val DarkOnPrimaryContainer = Color(0xFFFFFFFF)

val DarkSecondary = Color(0xFF03DAC6)     // Another accent color
val DarkOnSecondary = Color(0xFF000000)   // Text/icons on Secondary color

val DarkSecondaryContainer = Color(0xFF018786)
val DarkOnSecondaryContainer = Color(0xFFFFFFFF)

val DarkTertiary = Color(0xFF3700B3)      // Optional third accent
val DarkOnTertiary = Color(0xFFFFFFFF)

val DarkTertiaryContainer = Color(0xFF3700B3) // Usually same as Tertiary for simplicity unless needed
val DarkOnTertiaryContainer = Color(0xFFFFFFFF)

val DarkError = Color(0xFFCF6679)
val DarkOnError = Color(0xFF000000)

val DarkErrorContainer = Color(0xFFB00020)
val DarkOnErrorContainer = Color(0xFFFFFFFF)

// OLED Dark Theme Specifics
val DarkBackground = Color(0xFF000000)    // True black for main background
val DarkOnBackground = Color(0xFFE0E0E0)  // Light grey for text/icons on background (not pure white for less strain)

val DarkSurface = Color(0xFF000000)       // True black for surfaces like cards, dialogs, bottom sheets
val DarkOnSurface = Color(0xFFE0E0E0)     // Light grey for text/icons on surfaces

val DarkSurfaceVariant = Color(0xFF121212) // Slightly off-black for subtle variations, e.g., TextField background, dividers
val DarkOnSurfaceVariant = Color(0xFFBDBDBD) // Grey for text/icons on surface variants

val DarkOutline = Color(0xFF424242)       // For borders, dividers if not using SurfaceVariant

// You might not need all of these, but it's good to have them defined.
// The key ones for OLED dark mode are DarkBackground, DarkSurface, DarkOnBackground, DarkOnSurface,
// and your accent (Primary/Secondary).