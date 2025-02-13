package com.example.fantasystocks.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fantasystocks.API.StockApiService
import com.example.fantasystocks.API.StockResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Calendar

@Serializable
object Stocks

fun NavGraphBuilder.stocksDestination() {
    composable<Stocks> { StocksScreen() }
}

@Composable
fun StocksScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Watchlist") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Stocks") }
            )
        }

        when (selectedTab) {
            0 -> WatchlistTab()
            1 -> StocksTab()
        }
    }
}

@Composable
private fun WatchlistTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(3) { index ->
            StockItem(
                when (index) {
                    0 -> Triple("Microsoft", "MSFT", "$259.99")
                    1 -> Triple("Amazon", "AMZN", "$3,378.00")
                    else -> Triple("Tesla", "TSLA", "$678.90")
                }
            )
        }
    }
}

@Composable
private fun StocksTab() {
    val stocks = listOf("AAPL" to "Apple", "MSFT" to "Microsoft", "NVDA" to "Nvidia")
    val stockDataMap = remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val today = getCurrentDate()
        val yesterday = getYesterdayDate()

        val stockPrices = fetchStockPrices(stocks, yesterday, today)
        stockDataMap.value = stockPrices
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (stockDataMap.value.isEmpty()) {
                item { Text("No stock data available", modifier = Modifier.padding(16.dp)) }
            } else {
                items(stocks.size) { index ->
                    val (ticker, name) = stocks[index]
                    val price = stockDataMap.value[ticker]?.let { String.format(Locale.US, "%.2f", it) } ?: "N/A"
                    StockItem(Triple(name, ticker, "$$price"))
                }
            }
        }
    }
}

// Function to fetch stock prices for multiple tickers asynchronously
private suspend fun fetchStockPrices(stocks: List<Pair<String, String>>, fromDate: String, toDate: String): Map<String, Double> {
    return coroutineScope {
        val deferredResults = stocks.map { (ticker, _) ->
            async { ticker to StockApiService.getStockData(ticker, fromDate, toDate)?.results?.lastOrNull()?.closePrice }
        }
        deferredResults.awaitAll().filter { it.second != null }.associate { it.first to it.second!! }
    }
}

private fun getCurrentDate(): String {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return dateFormat.format(calendar.time)
}

private fun getYesterdayDate(): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -1) // Subtract one day
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return dateFormat.format(calendar.time)
}

@Composable
private fun StockItem(stock: Triple<String, String, String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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