package com.example.fantasystocks.models

import kotlinx.serialization.Serializable

@Serializable
data class UserInformation(
    val uid: String, // UUID as string
    val username: String
)

@Serializable
data class UserSettings(
    val uid: String, // UUID as string
    val dark_mode: Boolean = false,
    val notification_enabled: Boolean = true
) 