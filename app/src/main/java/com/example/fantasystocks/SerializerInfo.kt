package com.example.fantasystocks

import kotlinx.serialization.Serializable

// Data model for API response
@Serializable
data class StockResponse(
    val ticker: String,
    val results: List<StockData>?
)

@Serializable
data class StockData(
    val o: Double, // Open price
    val c: Double, // Close price
    val h: Double, // High price
    val l: Double  // Low price
)