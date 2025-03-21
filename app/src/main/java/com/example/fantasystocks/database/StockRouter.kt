package com.example.fantasystocks.database

import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import android.util.Log
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

@Serializable
data class UserStockData(
    val uid: String,
    @SerialName("league_id")
    val leagueId: Int,
    val quantity: Double,
    @SerialName("stock_id")
    val stockId: Int,
)

@Serializable
data class StockData(
    @SerialName("stock_id")
    val id: Int,
    val name: String,
    val ticker: String
)

@Serializable
data class HistoricalStockPrice(
    @SerialName("stock_id")
    val stockId: Int,
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val timestamp: String
)

@Serializable
data class StockDetails(
    val ticker: String,
    val id: Int,
    val priceHistory: List<Double>,
    val latestPrice: Double,
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double
)

@Serializable
data class StockWithPrices(
    @SerialName("stock_id")
    val stockId: Int,
    val ticker: String,
    @SerialName("historical_stock_price")
    val historicalStockPrice: List<HistoricalStockPrice>
)

object StockRouter {
    private val databaseConnector = SupabaseClient
    private const val PORTFOLIO_TABLE_NAME = "portfolio"
    private const val STOCK_TABLE_NAME = "stock"

    suspend fun getStockQuantity(leagueId: Int, uid: String, stockId: Int): Double {
        try {
            val stockObject = databaseConnector.supabase.from(PORTFOLIO_TABLE_NAME).select() {
                filter {
                    eq("uid", uid)
                    eq("league_id", leagueId)
                    eq("stock_id", stockId)
                }
            }.decodeSingleOrNull<UserStockData>()

            return stockObject?.quantity ?: 0.0

        } catch (e: Exception) {
            println("ERROR: $e")
            // should never reach here
            throw e
        }
    }

    suspend fun updateSessionStockQuantity(uid: String, leagueId: Int, stockId: Int, newQuantity: Double) {
        // Check if the stock already exists in the portfolio
        val existingStock = databaseConnector.supabase.from(PORTFOLIO_TABLE_NAME).select {
            filter {
                eq("uid", uid)
                eq("league_id", leagueId)
                eq("stock_id", stockId)
            }
        }.decodeSingleOrNull<UserStockData>()

        if (existingStock != null) {
            databaseConnector.supabase.from(PORTFOLIO_TABLE_NAME)
                .update({
                    set("quantity", newQuantity)
                }) {
                    filter {
                        eq("league_id", leagueId)
                        eq("uid", uid)
                        eq("stock_id", stockId)
                    }
                }
        } else {
            // If the stock doesn't exist, insert a new record
            databaseConnector.supabase.from(PORTFOLIO_TABLE_NAME)
                .insert(UserStockData(uid, leagueId, newQuantity, stockId)) {
                    select()
                }
        }
    }

    suspend fun getAvailableStocks(): List<StockDetails> {
        try {
            val columns = Columns.raw(
                """
                    *,
                    historical_stock_price!inner(
                        stock_id,
                        close,
                        open,
                        high,
                        low,
                        timestamp
                    )
                """.trimIndent()
            )

            val stocksWithPrices = databaseConnector.supabase
                .from(STOCK_TABLE_NAME)
                .select(columns = columns) {
                    limit(count = 30)
                }

            // Add detailed debugging
            Log.d("StockRouter", "Raw response: ${stocksWithPrices.data.toString()}")
            
            val decodedStocksWithPrices = stocksWithPrices.decodeList<StockWithPrices>()
            Log.d("StockRouter", "Decoded stocks: $decodedStocksWithPrices")

            return decodedStocksWithPrices
                .groupBy { it.stockId }
                .map { (_, stockGroup) ->
                    val sortedGroup = stockGroup.sortedByDescending { stock -> 
                        stock.historicalStockPrice.first().timestamp
                    }
                    val stock = sortedGroup.first()
                    val latestPriceData = stock.historicalStockPrice.first()
                    StockDetails(
                        ticker = stock.ticker,
                        id = stock.stockId,
                        priceHistory = stock.historicalStockPrice.map { it.close },
                        latestPrice = latestPriceData.close,
                        open = latestPriceData.open,
                        close = latestPriceData.close,
                        high = latestPriceData.high,
                        low = latestPriceData.low
                    )
                }
        } catch (e: Exception) {
            Log.e("StockRouter", "Error fetching stock details", e)
            throw e
        }
    }


    suspend fun getStockDetails(ticker: String): StockDetails {
        try {
            // First get the stock basic info
            val stock = databaseConnector.supabase
                .from(STOCK_TABLE_NAME)
                .select() {
                    filter {
                        eq("ticker", ticker)
                    }
                }
                .decodeSingle<StockData>()

            // Then get the historical prices
            val prices = databaseConnector.supabase
                .from("historical_stock_price")
                .select() {
                    filter {
                        eq("stock_id", stock.id)
                    }
                    limit(count = 30) // Get last 30 data points
                }
                .decodeList<HistoricalStockPrice>()

            // Get the latest price data
            val latestPrice = prices.firstOrNull() ?: throw Exception("No price data available")

            return StockDetails(
                ticker = stock.ticker,
                id = stock.id,
                priceHistory = prices.map { it.close },
                latestPrice = latestPrice.close,
                open = latestPrice.open,
                close = latestPrice.close,
                high = latestPrice.high,
                low = latestPrice.low
            )
        } catch (e: Exception) {
            Log.e("StockRouter", "Error fetching stock details", e)
            throw e
        }
    }

    suspend fun getStockPriceMap(stockIds: List<Int>): Map<Int, Double> {
        try {
            val stocks = databaseConnector.supabase
                .from("historical_stock_price")
                .select {
                    filter {
                        isIn("stock_id", stockIds)
                    }
                    order("timestamp", Order.DESCENDING)
                }
                .decodeList<HistoricalStockPrice>()

            // Group by stock_id and take the most recent price for each stock
            return stocks.groupBy { it.stockId }
                .mapValues { (_, prices) -> 
                    prices.firstOrNull()?.close ?: 0.0 
                }
        } catch (e: Exception) {
            Log.e("StockRouter", "Error fetching stock price map", e)
            throw e
        }
    }
}