package com.example.fantasystocks.classes

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val name: String,
    val password: String
)
