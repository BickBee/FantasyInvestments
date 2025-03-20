package com.example.fantasystocks.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.fantasystocks.ui.components.StockGraph
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
            0 -> WatchlistTab(goToStockViewer)
            1 -> StocksTab(goToStockViewer)
        }
    }
}

@Composable
private fun WatchlistTab(goToStockViewer: (String) -> Unit) {
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
                },
                goToStockViewer
            )
        }
    }
}

@Composable
private fun StocksTab(goToStockViewer: (String) -> Unit) {
    val stocks = listOf("AAPL" to "Apple", "MSFT" to "Microsoft", "NVDA" to "Nvidia")
    val stockDataMap = remember { mutableStateOf<Map<String, List<Double>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val today = getCurrentDate()
        val weekAgo = getDateMinusDays(7)
        
        val stockPrices = fetchStockPricesWithHistory(stocks, weekAgo, today)
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
                    val prices = stockDataMap.value[ticker] ?: emptyList()
                    val currentPrice = prices.lastOrNull()?.let { 
                        String.format(Locale.US, "%.2f", it) 
                    } ?: "N/A"
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = name, style = MaterialTheme.typography.titleMedium)
                                    Text(text = ticker, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(text = "$$currentPrice", style = MaterialTheme.typography.titleMedium)
                            }
                            
                            if (prices.isNotEmpty()) {
                                StockGraph(
                                    stockData = prices,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .padding(top = 16.dp)
                                )
                            }
                        }
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