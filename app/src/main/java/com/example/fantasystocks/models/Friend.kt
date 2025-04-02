package com.example.fantasystocks.models

import kotlinx.serialization.Serializable

@Serializable
data class Friend(
    val id: String, // UUID as string
    val username: String,
    val status: FriendStatus = FriendStatus.ACCEPTED,
    val avatarId: Int = 0
)

enum class FriendStatus {
    PENDING,
    ACCEPTED
}

@Serializable
data class FriendRequest(
    val user_id: String,
    val friend_id: String,
    val status: String
)

@Serializable
data class FriendRequestResponse(
    val user_id: String,
    val friend_id: String,
    val status: String
)