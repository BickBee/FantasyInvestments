package com.example.fantasystocks.classes

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class TxnType {
    BUY,
    SELL
}

@Serializable
data class Transaction(
    @SerialName("txn_id")
    val txnId: Int? = null,
    val uid: String,
    @SerialName("league_id")
    val leagueId: Int,
    @SerialName("stock_id")
    val stockId: Int,
    @SerialName("stock_name")
    val stockName: String,
    val action: TxnType,
    val quantity: Double,
    val price: Double,
    val timestamp: String
)