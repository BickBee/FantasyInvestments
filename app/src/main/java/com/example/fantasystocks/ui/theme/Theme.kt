package com.example.fantasystocks.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val InvalidRed = Color(0xFFE86563)
val PositiveGreen = Color(0XFF07AB25)

// Custom dark color scheme that matches our brand
private val CustomDarkColorScheme = darkColorScheme(
    // Primary - dark navy blue for trust and stability
    primary = Color(0xFF4A5D75),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1A2C42),
    onPrimaryContainer = Color(0xFFCFD8E2),

    // Secondary - darker teal for a modern financial feel
    secondary = Color(0xFF1A7D75),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF053E39),
    onSecondaryContainer = Color(0xFFB2DFDB),

    // Tertiary - darker gold for premium feel
    tertiary = Color(0xFFB09030),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF5C4B18),
    onTertiaryContainer = Color(0xFFFAF1D6),

    // Background colors - dark mode suitable
    background = Color(0xFF121212),
    onBackground = Color(0xFFE3E3E3),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE3E3E3),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFB8B8B8),

    // Error - traditional red for warnings/losses
    error = Color(0xFFE57373),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF8B0000),
    onErrorContainer = Color(0xFFFFDBD0)
)

// Original Light Color Scheme
private val CustomLightColorScheme = lightColorScheme(
    // Primary - deep navy blue for trust and stability
    primary = Color(0xFF1A2C42),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFD8E2),
    onPrimaryContainer = Color(0xFF0D1829),

    // Secondary - teal for a modern financial feel
    secondary = Color(0xFF26A69A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00403B),

    // Tertiary - subtle gold for premium feel
    tertiary = Color(0xFFD4AF37),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFFFAF1D6),
    onTertiaryContainer = Color(0xFF3D3000),

    // Background colors - clean and minimal
    background = Color(0xFFF9FAFC),
    onBackground = Color(0xFF202124),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF202124),
    surfaceVariant = Color(0xFFEEF0F4),
    onSurfaceVariant = Color(0xFF44474E),

    // Error - traditional red for warnings/losses
    error = Color(0xFFD84315),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDBD0),
    onErrorContainer = Color(0xFF3F0300)
)

@Composable
fun FantasyStocksTheme(
    // If darkTheme parameter is not provided, check the ThemeManager first,
    // then fall back to system theme setting
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    // Use the ThemeManager's state if no explicit parameter is provided
    val themeManagerDarkMode by ThemeManager.isDarkTheme.collectAsState()
    
    // Priority: 1. Explicit parameter, 2. ThemeManager, 3. System default
    val isDarkTheme = darkTheme ?: themeManagerDarkMode ?: isSystemInDarkTheme()
    
    val colorScheme = if (isDarkTheme) CustomDarkColorScheme else CustomLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
