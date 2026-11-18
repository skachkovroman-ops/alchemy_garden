package com.example.alchemygarden.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val WarmParchment = Color(0xFFF6F1E3)
val SoftCream = Color(0xFFFFFDF8)
val MossGreen = Color(0xFF4A7C59)
val DeepForest = Color(0xFF2D4739)
val TerracottaClay = Color(0xFFC87D55)
val GoldenHarvest = Color(0xFFE6A132)
val MutedInk = Color(0xFF3A352F)

private val CozyColorScheme = lightColorScheme(
    primary = MossGreen,
    onPrimary = Color.White,
    secondary = TerracottaClay,
    onSecondary = Color.White,
    tertiary = GoldenHarvest,
    background = WarmParchment,
    surface = SoftCream,
    onBackground = MutedInk,
    onSurface = MutedInk
)

@Composable
fun AlchemyGardenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CozyColorScheme,
        content = content
    )
}
