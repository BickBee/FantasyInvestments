package com.example.fantasystocks.models

import org.junit.Test
import org.junit.Assert.*

class FriendTest {
    @Test
    fun testFriendCreation() {
        val friend = Friend(
            id = "123",
            username = "testUser",
            status = FriendStatus.ACCEPTED
        )
        
        assertEquals("123", friend.id)
        assertEquals("testUser", friend.username)
        assertEquals(FriendStatus.ACCEPTED, friend.status)
    }

    @Test
    fun testFriendStatusEnum() {
        assertEquals("PENDING", FriendStatus.PENDING.toString())
        assertEquals("ACCEPTED", FriendStatus.ACCEPTED.toString())
    }

    @Test
    fun testFriendRequestCreation() {
        val request = FriendRequest(
            user_id = "user123",
            friend_id = "friend456",
            status = "PENDING"
        )
        
        assertEquals("user123", request.user_id)
        assertEquals("friend456", request.friend_id)
        assertEquals("PENDING", request.status)
    }

    @Test
    fun testFriendRequestResponseCreation() {
        val response = FriendRequestResponse(
            user_id = "user123",
            friend_id = "friend456",
            status = "ACCEPTED"
        )
        
        assertEquals("user123", response.user_id)
        assertEquals("friend456", response.friend_id)
        assertEquals("ACCEPTED", response.status)
    }
} 