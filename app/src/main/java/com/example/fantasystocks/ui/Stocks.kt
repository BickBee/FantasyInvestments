package com.example.fantasystocks.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fantasystocks.API.StockScreenAPI
import kotlinx.serialization.Serializable

@Serializable
object Stocks

fun NavGraphBuilder.stocksDestination() {
    composable<Stocks> {
        StockScreenAPI()
        GraphChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            dataPoints = listOf(
                1f to 5f,
                2f to 10f,
                3f to 15f,
                4f to 8f,
                5f to 20f
            ),
            lineColor = Color.Green,
            pointColor = Color.Red,
            strokeWidth = 4f,
            pointRadius = 6f
        )
    }
}

@Composable
fun StocksScreen() {
    Text("Hello from stocks")
}