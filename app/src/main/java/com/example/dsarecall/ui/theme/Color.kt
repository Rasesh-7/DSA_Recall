package com.example.dsarecall.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Obsidian Lilac Precision (Dark Theme Tokens from User Image 1)
val ObsidianBackground = Color(0xFF121015)
val ObsidianSurface = Color(0xFF1B1822)
val ObsidianSurfaceVariant = Color(0xFF25202E)
val ObsidianBorder = Color(0xFF3B3347)
val ObsidianPrimary = Color(0xFFD8C8FF)
val ObsidianSecondary = Color(0xFFC084FC)
val ObsidianTertiary = Color(0xFF2D2836)
val ObsidianTextPrimary = Color(0xFFF4F0FF)
val ObsidianTextSecondary = Color(0xFFB8B0C8)
val ObsidianTextMuted = Color(0xFF7E778E)

// Neomorphic Lilac Precision (Light Theme Tokens from User Image 2)
val NeomorphicBackground = Color(0xFFFAF0EB)
val NeomorphicSurface = Color(0xFFFFF8F5)
val NeomorphicSurfaceVariant = Color(0xFFF0E5DF)
val NeomorphicBorder = Color(0xFFE3D4CB)
val NeomorphicPrimary = Color(0xFFA47CA5)
val NeomorphicSecondary = Color(0xFFD8C8FF)
val NeomorphicTertiary = Color(0xFFF0E9FF)
val NeomorphicTextPrimary = Color(0xFF2A1E1A)
val NeomorphicTextSecondary = Color(0xFF635359)
val NeomorphicTextMuted = Color(0xFF9C8B92)

// Dynamic Composable Color Accessors for Theme Adaptability across all UI Components
val MinimalistBackground: Color
    @Composable get() = MaterialTheme.colorScheme.background

val MinimalistSurface: Color
    @Composable get() = MaterialTheme.colorScheme.surface

val MinimalistSurfaceVariant: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant

val MinimalistBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outline

val SapphirePrimary: Color
    @Composable get() = MaterialTheme.colorScheme.primary

val SlateSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.secondary

val TextPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

val TextMuted: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

// Status & Difficulty Colors (Harmonized for both Light & Dark themes)
val DifficultyEasy = Color(0xFF10B981)
val DifficultyMedium = Color(0xFFF59E0B)
val DifficultyHard = Color(0xFFF43F5E)

val SolitaryGreen = Color(0xFF10B981)
val HintYellow = Color(0xFFF59E0B)
val SolutionOrange = Color(0xFFFB923C)
val FailedRed = Color(0xFFF43F5E)