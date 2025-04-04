package com.example.fantasystocks.classes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInformationWithAvatar(
    val uid: String, // UUID as string
    val username: String,
    @SerialName("avatar_id")
    val avatarId: Int
)

@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val name: String,
    val password: String
)
