package com.example.nammanala.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CanalGreen       = Color(0xFF1B5E20)
val CanalGreenLight  = Color(0xFF388E3C)
val CanalTeal        = Color(0xFF00796B)
val WaterBlue        = Color(0xFF0288D1)
val WaterBluePale    = Color(0xFFB3E5FC)
val SurfaceLight     = Color(0xFFF1F8E9)
val ErrorRed         = Color(0xFFD32F2F)
val WarningAmber     = Color(0xFFF57C00)

private val LightColorScheme = lightColorScheme(
    primary          = CanalGreen,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    secondary        = CanalTeal,
    onSecondary      = Color.White,
    tertiary         = WaterBlue,
    background       = SurfaceLight,
    surface          = Color.White,
    onBackground     = Color(0xFF1C1B1F),
    onSurface        = Color(0xFF1C1B1F),
    error            = ErrorRed,
)

@Composable
fun NammaNalaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography   = AppTypography,
        content      = content
    )
}