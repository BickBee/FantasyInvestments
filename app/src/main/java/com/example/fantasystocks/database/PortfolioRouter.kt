package com.example.fantasystocks.database

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HistoricalPortfolioValue(
        val uid: String,
        val value: Double,
        @SerialName("league_id") val leagueId: Int,
)

object PortfolioRouter {
    private val databaseConnector = SupabaseClient
    private const val HISTORICAL_PORTFOLIO_TABLE_NAME = "historical_portfolio_value"

    private data class CachedValue(val value: Double, val timestamp: Long)
    private val portfolioCache = mutableMapOf<Pair<String, Int>, CachedValue>()
    private const val CACHE_TTL_MS = 10_000L // 10 seconds TTL

    private fun roundToTwoDecimalPlaces(value: Double): Double {
        return (value * 100.0).toInt() / 100.0
    }

    suspend fun getLatestPortfolioValue(uid: String, leagueId: Int): Double? {
        val cacheKey = Pair(uid, leagueId)
        val currentTime = System.currentTimeMillis()

        // Check cache first
        portfolioCache[cacheKey]?.let { cached ->
            if (currentTime - cached.timestamp < CACHE_TTL_MS) {
                return cached.value
            }
        }

        // If cache miss or expired, fetch from database
        val value = getHistoricalPortfolioValues(uid, leagueId).lastOrNull()?.value

        // Update cache if value exists
        value?.let { portfolioCache[cacheKey] = CachedValue(it, currentTime) }

        return value
    }

    suspend fun getHistoricalPortfolioValues(
            uid: String,
            leagueId: Int
    ): List<HistoricalPortfolioValue> {
        try {
            // First get the stock basic info
            val portfolioValue =
                    databaseConnector
                            .supabase
                            .from(HISTORICAL_PORTFOLIO_TABLE_NAME)
                            .select() {
                                filter {
                                    eq("league_id", leagueId)
                                    eq("uid", uid)
                                }
                                order("timestamp", Order.ASCENDING)
                            }
                            .decodeList<HistoricalPortfolioValue>()

            return portfolioValue.map {
                HistoricalPortfolioValue(
                        uid = it.uid,
                        value = roundToTwoDecimalPlaces(it.value),
                        leagueId = it.leagueId
                )
            }
        } catch (e: Exception) {
            Log.e("StockRouter", "Error fetching stock details", e)
            throw e
        }
    }
}
