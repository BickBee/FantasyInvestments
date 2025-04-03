package com.example.fantasystocks.ui.screens

import StocksTabViewModel
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fantasystocks.API.StockApiService
import com.example.fantasystocks.database.StockDetails
import com.example.fantasystocks.database.StockRouter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Serializable
object Stocks

@Serializable
data class Stock(val stock: String)

fun NavGraphBuilder.stocksDestination(goToStockViewer: (String) -> Unit) {
    composable<Stocks> { StocksScreen(goToStockViewer) }
}

@Composable
fun StockStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun StocksScreen(goToStockViewer: (String) -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        StocksTab(goToStockViewer)
    }
}

@Composable
private fun StocksTab(goToStockViewer: (String) -> Unit) {
    val viewModel = remember { StocksTabViewModel() }
    val state by viewModel.state.collectAsState()

    var filteredStockDetails by remember { mutableStateOf<List<StockDetails>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    // Fetch stocks on initial load
    LaunchedEffect(Unit) {
        try {
            filteredStockDetails = state.stockDetails
        } catch (e: Exception) {
            Log.e("StocksTab", "Error fetching stocks", e)
        }
    }

    // Filter stocks based on search query
    LaunchedEffect(searchQuery, state.stockDetails) {
        filteredStockDetails = if (searchQuery.isEmpty()) {
            state.stockDetails
        } else {
            state.stockDetails.filter { stock ->
                stock.ticker.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column {
        // Search TextField
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search stocks") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )

        // Rest of the existing code remains the same, but replace stockDetails with filteredStockDetails in the items block
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (filteredStockDetails.isEmpty()) {
                    item {
                        Text(
                            "No stocks found",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(filteredStockDetails.size) { index ->
                        val stock = filteredStockDetails[index]
                        StockItem(
                            Triple (
                                stock.ticker,
                                stock.ticker,
                                stock.latestPrice.toString()
                            ),
                            goToStockViewer
                        )
                    }
                }
            }
        }
    }
}

private fun getDateMinusDays(days: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -days)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return dateFormat.format(calendar.time)
}

private suspend fun fetchStockPricesWithHistory(
    stocks: List<Pair<String, String>>, 
    fromDate: String, 
    toDate: String
): Map<String, List<Double>> {
    return coroutineScope {
        val deferredResults = stocks.map { (ticker, _) ->
            async {
                val response = StockApiService.getStockData(ticker, fromDate, toDate)
                ticker to (response?.results?.map { it.closePrice } ?: emptyList())
            }
        }
        deferredResults.awaitAll().toMap()
    }
}

private fun getCurrentDate(): String {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return dateFormat.format(calendar.time)
}

@Composable
private fun StockItem(stock: Triple<String, String, String>, goToStockViewer: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { goToStockViewer(stock.second) }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = stock.first, style = MaterialTheme.typography.titleMedium)
                Text(text = stock.second, style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = stock.third, style = MaterialTheme.typography.titleMedium)
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