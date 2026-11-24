package com.vastra.store.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = Berry,
    onPrimary = Ivory,
    primaryContainer = BerryLight,
    onPrimaryContainer = BerryDark,
    secondary = Bronze,
    onSecondary = Ivory,
    secondaryContainer = BronzeLight,
    onSecondaryContainer = Color(0xFF2A1A00),
    tertiary = Teal,
    onTertiary = Ivory,
    background = Ivory,
    onBackground = Ink,
    surface = Ivory,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = Color(0xFF5A4A42),
    outline = Color(0xFF8C7A70),
    outlineVariant = Color(0xFFE0D2C8),
    error = Color(0xFFB3261E),
    onError = Ivory
)

private val DarkScheme = darkColorScheme(
    primary = BerryNight,
    onPrimary = BerryDark,
    primaryContainer = BerryNightContainer,
    onPrimaryContainer = BerryLight,
    secondary = BronzeNight,
    onSecondary = Color(0xFF2A1A00),
    secondaryContainer = Color(0xFF4A3617),
    onSecondaryContainer = BronzeLight,
    tertiary = Color(0xFF8FD5C6),
    onTertiary = Color(0xFF00382F),
    background = SurfaceNight,
    onBackground = IvoryNight,
    surface = SurfaceNight,
    onSurface = IvoryNight,
    surfaceVariant = SurfaceNightHigh,
    onSurfaceVariant = Color(0xFFD8C7C0),
    outline = Color(0xFF9C8A83),
    outlineVariant = Color(0xFF4A3B41),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun VastraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = VastraTypography,
        content = content
    )
}
