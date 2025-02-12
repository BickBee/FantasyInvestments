package com.example.fantasystocks

import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem<T>(val label: String, val icon: ImageVector, val route: T)