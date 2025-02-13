package com.example.fantasystocks.API

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

object StockApiService {
    private const val BASE_URL = "https://api.polygon.io/v2/aggs/ticker"
    private const val API_KEY = "V3H2llAJBWPwTeZtRA8_YkZpcy_3e_jr"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getStockData(ticker: String, fromDate: String, toDate: String): StockResponse? {
        val url = "$BASE_URL/$ticker/range/1/day/$fromDate/$toDate?adjusted=true&sort=asc&apiKey=$API_KEY"

        try
        {
            val response: HttpResponse = client.get(url)
            val stockResponse = response.body<StockResponse>()
            return stockResponse
        }
        catch (e: Exception)
        {
            return null
        }
    }
}

//@Composable
//fun StockScreenAPI(modifier: Modifier = Modifier) {
//    var stockInfo by remember { mutableStateOf("Loading...") }
//    val client = remember {
//        HttpClient {
//            install(ContentNegotiation) {
//                json(Json { ignoreUnknownKeys = true })
//            }
//        }
//    }
//    val coroutineScope = rememberCoroutineScope()
//
//    LaunchedEffect(Unit) {
//        coroutineScope.launch {
//            stockInfo = getStockData(client)
//        }
//    }
//
//    Column(modifier = modifier.padding(16.dp)) {
//        Text(text = stockInfo, style = MaterialTheme.typography.bodyLarge)
//    }
//}
//
//suspend fun getStockData(client: HttpClient): String {
//    val url = "https://api.polygon.io/v2/aggs/ticker/AAPL/range/1/day/2025-01-01/2025-01-20?adjusted=true&sort=asc&apiKey=V3H2llAJBWPwTeZtRA8_YkZpcy_3e_jr"
//
//    return try {
//        val response: HttpResponse = client.get(url)
//        val stockResponse = response.body<StockResponse>()
//        stockResponse.results?.joinToString("\n") {
//            "Open: ${it.o}, Close: ${it.c}, High: ${it.h}, Low: ${it.l}"
//        } ?: "No data found"
//    } catch (e: Exception) {
//        "Failed to fetch data"
//    }
//}