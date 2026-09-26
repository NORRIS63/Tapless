package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RecordingRed,
    onPrimary = Color.White,
    primaryContainer = RecordingRedContainer,
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = PlaybackCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = PlaybackCyanContainer,
    onSecondaryContainer = Color(0xFFBCE9F1),
    tertiary = StatusGreen,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceBorder,
    outlineVariant = Color(0xFF333846)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to clean dark studio theme as requested
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
