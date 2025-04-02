package com.example.fantasystocks.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton manager for theme settings across the app
 * This ensures consistent theme application across all screens
 */
object ThemeManager {
    // Internal mutable state for dark mode preference
    private val _isDarkTheme = MutableStateFlow(false)
    
    // Public immutable state flow for observers
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()
    
    // Update the dark mode setting
    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }
}