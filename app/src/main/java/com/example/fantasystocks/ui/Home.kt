package com.example.fantasystocks.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object Home

fun NavGraphBuilder.homeDestination() {
    composable<Home> { HomeScreen() }
}

@Composable
fun HomeScreen() {
    Text("Hello from home")
}