package com.example.fantasystocks.classes

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

enum class TxnType {
    BUY,
    SELL
}

@Serializable
data class Transaction(
    val id: Int? = null,
    val type: TxnType,
    val stock: String, // stock id when implemented with db
    val amount: Double,
    val date: LocalDate
)