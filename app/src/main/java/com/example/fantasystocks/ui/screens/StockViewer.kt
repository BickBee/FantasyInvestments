package com.example.fantasystocks.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fantasystocks.TRANSACTION_FEE
import com.example.fantasystocks.database.StockRouter
import com.example.fantasystocks.ui.components.StockGraph
import com.example.fantasystocks.ui.theme.InvalidRed
import com.example.fantasystocks.ui.viewmodels.NO_SESSION_SELECTED
import com.example.fantasystocks.ui.viewmodels.StockViewerViewModel

// Enum to represent date range options
enum class DateRange(val label: String, val days: Int) {
    WEEK("1 Week", 7),
    MONTH("1 Month", 30),
    THREE_MONTHS("3 Months", 90)
}

fun NavGraphBuilder.stockViewer() {
    composable<Stock> { backStackEntry ->
        val stock = backStackEntry.arguments?.getString("stock") ?: "Unknown"
        StockViewer(stock, StockViewerViewModel(stock))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockViewer(stockTicker: String, viewModel: StockViewerViewModel) {
    val state by viewModel.state.collectAsState()
    val allStockPrices = remember { mutableStateOf<List<Double>>(emptyList()) }
    val displayedStockPrices = remember { mutableStateOf<List<Double>>(emptyList()) }
    val selectedDateRange = remember { mutableStateOf(DateRange.MONTH) }
    val scrollState = rememberScrollState()
    val stockPrice = state.latestPrice * if (state.quantity.isEmpty()) 0.0 else state.quantity.toDouble()
    val totalTransactionFee = stockPrice * TRANSACTION_FEE

    LaunchedEffect(Unit) {
        viewModel.getLeagues()
    }

    LaunchedEffect(state.selectedSession) {
        allStockPrices.value = StockRouter.getAllHistoricalClosingPrices(stockTicker)
        updateDisplayedPrices(allStockPrices.value, selectedDateRange.value, displayedStockPrices)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Stock Header
        Text(
            text = stockTicker,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Stock Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StockStat("Price", state.latestPrice.toString())
            StockStat("Low", state.stockLow.toString())
            StockStat("High", state.stockHigh.toString())
            StockStat("Open", state.stockOpen.toString())
            StockStat("Close", state.stockClose.toString())
        }

        // Stock Graph
        if (displayedStockPrices.value.isNotEmpty()) {
            StockGraph(
                stockData = displayedStockPrices.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(top = 16.dp)
            )
        }

        // Date Range Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateRange.values().forEach { range ->
                val requiredDataPoints = when (range) {
                    DateRange.WEEK -> 5
                    DateRange.MONTH -> 20
                    DateRange.THREE_MONTHS -> 60
                }

                if (allStockPrices.value.size >= requiredDataPoints) {
                    FilterChip(
                        selected = selectedDateRange.value == range,
                        onClick = {
                            selectedDateRange.value = range
                            updateDisplayedPrices(allStockPrices.value, range, displayedStockPrices)
                        },
                        label = { Text(range.label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Trading Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Trading",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = state.isSessionDropdownExpanded,
                    onExpandedChange = { viewModel.updateSessionDropdownExpanded(it) }
                ) {
                    OutlinedTextField(
                        value = state.selectedSession,
                        onValueChange = {  },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor().clickable { viewModel.updateSessionDropdownExpanded(!state.isSessionDropdownExpanded) },
                        readOnly = true,
                        label = { Text("Session name") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isSessionDropdownExpanded)
                        },
                    )

                    ExposedDropdownMenu(
                        expanded = state.isSessionDropdownExpanded,
                        onDismissRequest = { viewModel.updateSessionDropdownExpanded(false) }
                    ) {
                        state.sessions.forEach { session ->
                            DropdownMenuItem(
                                text = { Text(session.name) },
                                onClick = { viewModel.updateSelectedSession(session) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                val balanceText = if (state.selectedSession == NO_SESSION_SELECTED) {
                    "N/A" // or any default text/value you want to show when no session is selected
                } else {
                    String.format("$%.2f", state.balance)
                }

                Text(
                    text = "Balance: $balanceText",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = "Stock Owned: ${String.format("%.2f", state.stockBalance)}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quantity Input
                TextField(
                    value = state.quantity,
                    onValueChange = { viewModel.updateQuantity(it) },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Transaction fee percentage: 0.5%")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Transaction Fee: $${String.format("%.2f", totalTransactionFee ?: 0)}")

                // Buy/Sell Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.createTransaction(true) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Buy")
                    }
                    Button(
                        onClick = { viewModel.createTransaction(false) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = InvalidRed, contentColor = Color.White)
                    ) {
                        Text("Sell")
                    }
                }
            }
        }
        state.message?.let { message ->
            Snackbar(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(message)
            }
        }
    }
}

// Helper function to update displayed prices based on selected date range
private fun updateDisplayedPrices(
    allPrices: List<Double>,
    dateRange: DateRange,
    displayedPrices: androidx.compose.runtime.MutableState<List<Double>>
) {
    // Calculate how many data points to show based on date range
    // Assuming 1 data point per trading day (approx. 20 per month)
    val dataPointsToShow = when (dateRange) {
        DateRange.WEEK -> 5 // Approximate trading days in a week
        DateRange.MONTH -> 20 // Approximate trading days in a month
        DateRange.THREE_MONTHS -> 60 // Approximate trading days in 3 months
    }

    // Limit the number of data points based on available data
    val actualPointsToShow = minOf(dataPointsToShow, allPrices.size)

    // Get the most recent data points
    displayedPrices.value = if (actualPointsToShow > 0) {
        allPrices.takeLast(actualPointsToShow)
    } else {
        emptyList()
    }
}