package com.example.fantasystocks.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val InvalidRed = Color(0xFFE86563)

private val DarkColorScheme = darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40

        /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun FantasyStocksTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        // Dynamic color is available on Android 12+
        dynamicColor: Boolean = true,
        content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
//    val colorScheme = CustomLightColorScheme

    MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
    )
}

private val CustomLightColorScheme = lightColorScheme(
    // Primary - deep navy blue for trust and stability
    primary = Color(0xFF1A2C42),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFD8E2),
    onPrimaryContainer = Color(0xFF0D1829),

    // Secondary - teal for a modern financial feel
    secondary = Color(0xFF26A69A),
    onSecondary = Color(0xFF899BD2),
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
