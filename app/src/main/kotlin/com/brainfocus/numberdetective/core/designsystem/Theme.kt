package com.brainfocus.numberdetective.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EliteNoirColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    secondary = PrimaryPurple,
    tertiary = SecondaryBlue,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed
)

@Composable
fun NumberDetectiveTheme(
    darkTheme: Boolean = true, // Forced dark theme for the premium noir experience
    content: @Composable () -> Unit
) {
    // We strictly use the Elite Noir palette as requested for a premium experience
    MaterialTheme(
        colorScheme = EliteNoirColorScheme,
        typography = Typography,
        content = content
    )
}
