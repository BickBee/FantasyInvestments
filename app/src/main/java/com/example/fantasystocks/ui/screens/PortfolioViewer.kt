package com.example.fantasystocks.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fantasystocks.classes.Stock
import com.example.fantasystocks.classes.Player
import kotlinx.serialization.Serializable
import androidx.compose.foundation.clickable

@Serializable
data class Portfolio(val sessionId: String)

fun NavGraphBuilder.portfolioViewer(goToStockViewer: (String) -> Unit) {
    composable<Portfolio> { backStackEntry ->
        val sessionId = backStackEntry.arguments?.getString("sessionId") ?: "Unknown"
        PortfolioViewer(sessionId, goToStockViewer)
    }
}

@Composable
fun PortfolioViewer(sessionId: String, goToStockViewer: (String) -> Unit) {
    // Dummy data for demonstration
    val dummyStocks = remember {
        mapOf(
            "AAPL" to Pair(5, 180.0),
            "GOOGL" to Pair(2, 140.0),
            "MSFT" to Pair(8, 390.0)
        )
    }
    
    val initialBalance = 10000.0
    val currentBalance = 5000.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Session: $sessionId",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Portfolio Summary
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Portfolio Summary",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Initial Balance")
                        Text("$${String.format("%.2f", initialBalance)}")
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Current Balance")
                        Text("$${String.format("%.2f", currentBalance)}")
                    }
                }
            }
        }

        // Holdings
        Text(
            "Your Holdings",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn {
            items(dummyStocks.entries.toList()) { (ticker, details) ->
                val (quantity, price) = details
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { goToStockViewer(ticker) },
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = ticker, style = MaterialTheme.typography.titleMedium)
                            Text(text = "$quantity shares", style = MaterialTheme.typography.bodyMedium)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$${String.format("%.2f", price)}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Total: $${String.format("%.2f", price * quantity)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Buy More Stocks Button
        Button(
            onClick = { goToStockViewer("") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Buy More Stocks")
        }
    }
}
