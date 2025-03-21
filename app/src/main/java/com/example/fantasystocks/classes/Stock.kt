package com.example.fantasystocks.classes

import kotlinx.serialization.Serializable

@Serializable
data class Stock (
    val id: Int,
    val name: String,
    val ticker: String,
    val price: Double = 125.0
)