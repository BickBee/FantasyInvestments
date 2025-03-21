package com.example.fantasystocks.classes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserLeague(
    val uid: String?,
    @SerialName("league_id")
    val leagueId: Int?,
    val cash: Double,
    @SerialName("initial_value")
    val initValue: Double
)