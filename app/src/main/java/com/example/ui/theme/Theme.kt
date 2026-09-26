package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TealAccent,
    onPrimary = Color(0xFF101114),
    primaryContainer = TealAccentContainer,
    onPrimaryContainer = Color(0xFFD0F9F1),
    secondary = TealAccent,
    onSecondary = Color(0xFF101114),
    secondaryContainer = TealAccentContainer,
    onSecondaryContainer = Color(0xFFD0F9F1),
    tertiary = StatusGreen,
    error = RecordingRed,
    onError = Color.White,
    errorContainer = RecordingRedContainer,
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceBorder,
    outlineVariant = DarkDivider
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

