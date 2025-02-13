package com.example.fantasystocks.API

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StockResponse(
    val ticker: String,
    val adjusted: Boolean,
    val queryCount: Int,
    val request_id: String,
    val resultsCount: Int,
    val status: String,
    val results: List<StockData>?
)

@Serializable
data class StockData(
    @SerialName("o") val openPrice: Double,      // Open price
    @SerialName("c") val closePrice: Double,     // Close price
    @SerialName("h") val highPrice: Double,      // High price
    @SerialName("l") val lowPrice: Double,       // Low price
    @SerialName("t") val timestamp: Long,        // Unix Msec timestamp for the start of the aggregate window
    @SerialName("v") val volume: Double,         // Trading volume
    @SerialName("vw") val volumeWeightedPrice: Double?, // Volume weighted average price (nullable)
    @SerialName("n") val transactionCount: Int?  // Number of transactions (nullable)
)
