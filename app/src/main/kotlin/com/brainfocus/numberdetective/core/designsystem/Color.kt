package com.brainfocus.numberdetective.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// --- Core Palette ---
val PrimaryCyan = Color(0xFF00E5FF)
val PrimaryPurple = Color(0xFF6200EA)
val PrimaryBlue = Color(0xFF1E88E5)
val SecondaryBlue = Color(0xFF64B5F6)

val BackgroundDark = Color(0xFF070A0F)
val SurfaceDark = Color(0xFF121821)
val SurfaceCard = Color(0xBB1A222E) // Semi-transparent for glassmorphism

val TextPrimary = Color(0xFFE0E0E0)
val TextSecondary = Color(0xFFB0B0B0)

val SuccessGreen = Color(0xFF4CAF50)
val WarningYellow = Color(0xFFFFC107)
val ErrorRed = Color(0xFFF44336)
val GlowCyan = Color(0x6600E5FF)

// --- Gradients for "Metro-Catching" Effect ---
val PlayButtonGradient = Brush.horizontalGradient(
    colors = listOf(PrimaryCyan, Color(0xFF00B0FF), PrimaryBlue)
)

val CardBorderGradient = Brush.linearGradient(
    colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent, Color.White.copy(alpha = 0.1f))
)

val BackgroundOverlayGradient = Brush.verticalGradient(
    colors = listOf(Color.Transparent, BackgroundDark.copy(alpha = 0.8f), BackgroundDark)
)

val MissionTitleGradient = Brush.horizontalGradient(
    colors = listOf(PrimaryCyan, SecondaryBlue)
)
