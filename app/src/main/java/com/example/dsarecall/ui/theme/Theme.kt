package com.example.dsarecall.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Obsidian Lilac Precision (Dark Theme)
private val ObsidianDarkColorScheme = darkColorScheme(
    primary = ObsidianPrimary, // #D8C8FF
    onPrimary = ObsidianBackground, // #121015
    primaryContainer = ObsidianTertiary, // #2D2836
    onPrimaryContainer = ObsidianPrimary, // #D8C8FF
    secondary = ObsidianSecondary, // #C084FC
    onSecondary = ObsidianBackground, // #121015
    secondaryContainer = ObsidianSurfaceVariant, // #25202E
    onSecondaryContainer = ObsidianSecondary, // #C084FC
    tertiary = ObsidianTertiary, // #2D2836
    onTertiary = ObsidianTextPrimary, // #F4F0FF
    background = ObsidianBackground, // #121015
    onBackground = ObsidianTextPrimary, // #F4F0FF
    surface = ObsidianSurface, // #1B1822
    onSurface = ObsidianTextPrimary, // #F4F0FF
    surfaceVariant = ObsidianSurfaceVariant, // #25202E
    onSurfaceVariant = ObsidianTextSecondary, // #B8B0C8
    outline = ObsidianBorder // #3B3347
)

// Neomorphic Lilac Precision (Light Theme)
private val NeomorphicLightColorScheme = lightColorScheme(
    primary = NeomorphicPrimary, // #A47CA5
    onPrimary = Color.White,
    primaryContainer = NeomorphicTertiary, // #F0E9FF
    onPrimaryContainer = NeomorphicTextPrimary, // #2A1E1A
    secondary = NeomorphicSecondary, // #D8C8FF
    onSecondary = NeomorphicTextPrimary, // #2A1E1A
    secondaryContainer = NeomorphicSurfaceVariant, // #F0E5DF
    onSecondaryContainer = NeomorphicTextPrimary, // #2A1E1A
    tertiary = NeomorphicTertiary, // #F0E9FF
    onTertiary = NeomorphicTextPrimary, // #2A1E1A
    background = NeomorphicBackground, // #FAF0EB
    onBackground = NeomorphicTextPrimary, // #2A1E1A
    surface = NeomorphicSurface, // #FFF8F5
    onSurface = NeomorphicTextPrimary, // #2A1E1A
    surfaceVariant = NeomorphicSurfaceVariant, // #F0E5DF
    onSurfaceVariant = NeomorphicTextSecondary, // #635359
    outline = NeomorphicBorder // #E3D4CB
)

@Composable
fun DSARecallTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ObsidianDarkColorScheme else NeomorphicLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}