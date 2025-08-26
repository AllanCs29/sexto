package com.example.routineapp.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

enum class ThemeVariant { OLIVE, ARENA, CARBON }

private fun oliveScheme(dark: Boolean) = if (dark) darkColorScheme(
    primary = Olive, onPrimary = Color.White,
    secondary = Sage,
    background = Graphite, surface = Smoke,
    onBackground = Color(0xFFECECEC), onSurface = Color(0xFFECECEC)
) else lightColorScheme(
    primary = Olive, onPrimary = Color.White,
    secondary = Sage,
    background = Cloud, surface = Mist,
    onBackground = Color(0xFF111111), onSurface = Color(0xFF111111)
)

private fun arenaScheme(dark: Boolean) = if (dark) darkColorScheme(
    primary = Stone, onPrimary = Color.White,
    secondary = Dune,
    background = Graphite, surface = Smoke,
    onBackground = Color(0xFFECECEC), onSurface = Color(0xFFECECEC)
) else lightColorScheme(
    primary = Stone, onPrimary = Color.White,
    secondary = Dune,
    background = Cream, surface = Sand,
    onBackground = Color(0xFF1A1918), onSurface = Color(0xFF1A1918)
)

private fun carbonScheme(dark: Boolean) = if (dark) darkColorScheme(
    primary = Slate, onPrimary = Color.White,
    secondary = Silver,
    background = Charcoal, surface = Slate,
    onBackground = Color(0xFFECECEC), onSurface = Color(0xFFECECEC)
) else lightColorScheme(
    primary = Slate, onPrimary = Color.White,
    secondary = Silver,
    background = Color(0xFFF7F7F8), surface = Color(0xFFEDEFF1),
    onBackground = Color(0xFF121314), onSurface = Color(0xFF121314)
)

@Composable
fun RoutineTheme(variant: ThemeVariant, dark: Boolean, content: @Composable () -> Unit) {
    val scheme = when (variant) {
        ThemeVariant.OLIVE -> oliveScheme(dark)
        ThemeVariant.ARENA -> arenaScheme(dark)
        ThemeVariant.CARBON -> carbonScheme(dark)
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
