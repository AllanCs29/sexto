package com.example.routineapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeVariant(val label: String) { OLIVE("Olive"), ARENA("Arena"), CARBON("Carbón") }

private fun olive(dark: Boolean): ColorScheme = if (dark) darkColorScheme(
    primary = Color(0xFF6B7D57), secondary = Color(0xFF889476), background = Color(0xFF121212), surface = Color(0xFF1A1A1A)
) else lightColorScheme(
    primary = Color(0xFF6B7D57), secondary = Color(0xFF899B78), background = Color(0xFFF7F7F5), surface = Color(0xFFFFFFFF)
)
private fun arena(dark: Boolean): ColorScheme = if (dark) darkColorScheme(
    primary = Color(0xFF9A8C7A), secondary = Color(0xFFB9A995), background = Color(0xFF121212), surface = Color(0xFF1A1A1A)
) else lightColorScheme(
    primary = Color(0xFF9A8C7A), secondary = Color(0xFFC9BBA7), background = Color(0xFFFAF8F5), surface = Color(0xFFFFFFFF)
)
private fun carbon(dark: Boolean): ColorScheme = if (dark) darkColorScheme(
    primary = Color(0xFF5A5F66), secondary = Color(0xFF8B9199), background = Color(0xFF0E0F11), surface = Color(0xFF16181B)
) else lightColorScheme(
    primary = Color(0xFF5A5F66), secondary = Color(0xFF8B9199), background = Color(0xFFF5F6F7), surface = Color(0xFFFFFFFF)
)

@Composable
fun RoutineTheme(variant: ThemeVariant, dark: Boolean, content: @Composable () -> Unit) {
    val colors = when (variant) {
        ThemeVariant.OLIVE -> olive(dark)
        ThemeVariant.ARENA -> arena(dark)
        ThemeVariant.CARBON -> carbon(dark)
    }
    MaterialTheme(colorScheme = colors, typography = androidx.compose.material3.Typography(), content = content)
}
