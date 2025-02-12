package com.example.fantasystocks.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Serializable
object News

fun NavGraphBuilder.newsDestination() {
    composable<News> { NewsScreen() }
}

@Composable
fun NewsScreen() {
    Text("Hello from news")
}