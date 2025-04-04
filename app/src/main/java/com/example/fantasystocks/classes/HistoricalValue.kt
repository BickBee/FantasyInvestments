package com.example.fantasystocks.classes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HistoricalValue(
//    val uid: String,
//    @SerialName("league_id")
//    val leagueId: Int,
//    val timestamp: String,
    val value: Double
)
