package com.example.fantasystocks.database

import com.example.fantasystocks.classes.UserInformationWithAvatar
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.*
import com.example.fantasystocks.models.UserInformation
import com.example.fantasystocks.models.UserSettings
import com.example.fantasystocks.models.Friend
import com.example.fantasystocks.models.FriendRequest
import com.example.fantasystocks.models.FriendRequestResponse
import com.example.fantasystocks.models.FriendStatus

object SupabaseClient {
    private const val SUPABASE_URL = "https://lnfecoxuwybrlhzjqxkb.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxuZmVjb3h1d3licmxoempxeGtiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzg5NDQwMTMsImV4cCI6MjA1NDUyMDAxM30.wCjoCRqMTLOWyPxX-9lMohKbxESbP8z6G0FM2Gk3GLY"

        val supabase = createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Postgrest)
            install(Storage)
            install(Auth) {
                autoSaveToStorage = true
                autoLoadFromStorage = true
                alwaysAutoRefresh = true
            }
    }

    // Get authentication state
    fun isAuthenticated(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }

    suspend fun signUpNewUser(emailInput: String, passwordInput: String, username: String) {
        withContext(Dispatchers.IO) {
            try {
                // First check if the username is already taken
                val usernameExists = isUsernameAvailable(username)
                if (!usernameExists) {
                    throw Exception("Username already taken")
                }
                
                // Sign up the user in auth system
                supabase.auth.signUpWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                
                // Then add the username to user_information table
                val currentUser = getCurrentUser()
                if (currentUser != null) {
                    val userId = currentUser.id
                    insertUserInformation(userId, username)
                    initializeUserSettings(userId)
                } else {
                    throw Exception("Failed to create user account")
                }
            } catch (e: Exception) {
                throw Exception("Sign up failed: ${e.message}")
            }
        }
    }

    private suspend fun insertUserInformation(userId: String, username: String) {
        withContext(Dispatchers.IO) {
            try {
                supabase.from("user_information")
                    .insert(UserInformation(userId, username))
            } catch (e: Exception) {
                throw Exception("Failed to store user information: ${e.message}")
            }
        }
    }

    private suspend fun initializeUserSettings(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                supabase.from("user_settings")
                    .insert(UserSettings(userId))
            } catch (e: Exception) {
                println("Failed to initialize user settings somehow: ${e.message}")
            }
        }
    }

    suspend fun signInExistingUser(emailInput: String, passwordInput: String) {
        withContext(Dispatchers.IO) {
            supabase.auth.signInWith(Email) {
                email = emailInput
                password = passwordInput
            }
        }
    }

    suspend fun resetPasswordForEmail(email: String) {
        withContext(Dispatchers.IO) {
            supabase.auth.resetPasswordForEmail(email)
        }
    }

    suspend fun updatePassword(newPassword: String) {
        withContext(Dispatchers.IO) {
            supabase.auth.updateUser {
                password = newPassword
            }
        }
    }

    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            supabase.auth.signOut()
        }
    }

    fun getCurrentUser() = supabase.auth.currentSessionOrNull()?.user

    fun getCurrentUID() = getCurrentUser()?.id

    suspend fun getUsername(uid: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val result = supabase.from("user_information")
                    .select() {
                        filter {
                            eq("uid", uid)
                        }
                    }
                    .decodeSingleOrNull<UserInformation>()
                result?.username
            } catch (e: Exception) {
                println("Error getting username: ${e.message}")
                null
            }
        }
    }

    suspend fun updateUsername(uid: String, newUsername: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // First check if username is available
                if (!isUsernameAvailable(newUsername)) {
                    return@withContext false
                }
                
                // Update username
                supabase.from("user_information")
                    .update(mapOf("username" to newUsername)) {
                        filter {
                            eq("uid", uid)
                        }
                    }
                true
            } catch (e: Exception) {
                println("Error updating username: ${e.message}")
                false
            }
        }
    }

    suspend fun isUsernameAvailable(username: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val existingUsers = supabase.from("user_information")
                    .select() {
                        filter {
                            eq("username", username)
                        }
                    }
                    .decodeList<UserInformation>()
                existingUsers.isEmpty()
            } catch (e: Exception) {
                false
            }
        }
    }
    
    suspend fun getUserSettings(userId: String? = null): UserSettings? {
        val targetId = userId ?: getCurrentUser()?.id ?: return null
        
        return withContext(Dispatchers.IO) {
            try {
                supabase.from("user_settings")
                    .select() {
                        filter {
                            eq("uid", targetId)
                        }
                    }
                    .decodeSingleOrNull<UserSettings>()
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun updateUserSettings(settings: UserSettings): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                supabase.from("user_settings")
                    .update({
                        set("dark_mode", settings.dark_mode)
                        set("notification_enabled", settings.notification_enabled)
                        set("avatar_id", settings.avatar_id)
                    }) {
                        filter {
                            eq("uid", settings.uid)
                        }
                    }
                true
            } catch (e: Exception) {
                println("Error updating user settings: ${e.message}")
                false
            }
        }
    }

    suspend fun sendFriendRequest(friendUsername: String): Boolean {
        val currentUserId = getCurrentUser()?.id ?: return false
        
        return withContext(Dispatchers.IO) {
            try {
                // Get friend's ID using the helper function
                val friendId = getUserIdFromUsername(friendUsername) ?: return@withContext false
                
                // Check if request already exists
                val existingRequest = supabase.from("user_friends")
                    .select() {
                        filter {
                            eq("user_id", currentUserId)
                            eq("friend_id", friendId)
                        }
                    }
                    .decodeSingleOrNull<FriendRequest>()
                
                if (existingRequest != null) {
                    return@withContext false
                }
                
                // Create the friend request
                supabase.from("user_friends")
                    .insert(FriendRequest(
                        user_id = currentUserId,
                        friend_id = friendId,
                        status = "PENDING"
                    ))
                
                true
            } catch (e: Exception) {
                println("Error sending friend request: ${e.message}")
                false
            }
        }
    }

    suspend fun getFriends(): List<Friend> {
        val currentUserId = getCurrentUser()?.id ?: return emptyList()
        
        return withContext(Dispatchers.IO) {
            try {
                // Get all accepted friendships where current user is user_id
                val results = supabase.from("user_friends")
                    .select() {
                        filter {
                            eq("user_id", currentUserId)
                            eq("status", FriendStatus.ACCEPTED.toString())
                        }
                    }
                    .decodeList<FriendRequestResponse>()
                
                // Process results to get usernames and avatar IDs
                results.map { result ->
                    val friendId = result.friend_id
                    val username = getUsername(friendId) ?: "Unknown"
                    val settings = getUserSettings(friendId)
                    val avatarId = settings?.avatar_id ?: 0
                    
                    Friend(
                        id = friendId,
                        username = username,
                        status = FriendStatus.ACCEPTED,
                        avatarId = avatarId
                    )
                }
            } catch (e: Exception) {
                println("Error getting friends: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun getIncomingFriendRequests(): List<Friend> {
        val currentUserId = getCurrentUser()?.id ?: return emptyList()
        
        return withContext(Dispatchers.IO) {
            try {
                // Get all pending requests where current user is friend_id
                val results = supabase.from("user_friends")
                    .select() {
                        filter {
                            eq("friend_id", currentUserId)
                            eq("status", FriendStatus.PENDING.toString())
                        }
                    }
                    .decodeList<FriendRequestResponse>()
                
                // Process results to get usernames and avatar IDs
                results.map { result ->
                    val friendId = result.user_id
                    val username = getUsername(friendId) ?: "Unknown"
                    val settings = getUserSettings(friendId)
                    val avatarId = settings?.avatar_id ?: 0
                    
                    Friend(
                        id = friendId,
                        username = username,
                        status = FriendStatus.PENDING,
                        avatarId = avatarId
                    )
                }
            } catch (e: Exception) {
                println("Error getting incoming friend requests: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun getOutgoingFriendRequests(): List<Friend> {
        val currentUserId = getCurrentUser()?.id ?: return emptyList()
        
        return withContext(Dispatchers.IO) {
            try {
                // Get all pending requests where current user is user_id
                val results = supabase.from("user_friends")
                    .select() {
                        filter {
                            eq("user_id", currentUserId)
                            eq("status", FriendStatus.PENDING.toString())
                        }
                    }
                    .decodeList<FriendRequestResponse>()
                
                // Process results to get usernames and avatar IDs
                results.map { result ->
                    val friendId = result.friend_id
                    val username = getUsername(friendId) ?: "Unknown"
                    val settings = getUserSettings(friendId)
                    val avatarId = settings?.avatar_id ?: 0
                    
                    Friend(
                        id = friendId,
                        username = username,
                        status = FriendStatus.PENDING,
                        avatarId = avatarId
                    )
                }
            } catch (e: Exception) {
                println("Error getting outgoing friend requests: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun respondToFriendRequest(friendId: String, accept: Boolean): Boolean {
        val currentUserId = getCurrentUser()?.id ?: return false
        val status = if (accept) "ACCEPTED" else "REJECTED"
        
        return withContext(Dispatchers.IO) {
            try {
                supabase.from("user_friends")
                    .update({
                        set("status", status)
                    }) {
                        filter {
                            eq("user_id", friendId)
                            eq("friend_id", currentUserId)
                        }
                    }
                
                // If accepted, create the reverse relationship
                if (accept) {
                    supabase.from("user_friends")
                        .upsert(FriendRequest(
                            user_id = currentUserId,
                            friend_id = friendId,
                            status = "ACCEPTED"
                        ))
                }
                
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun removeFriend(friendId: String): Boolean {
        val currentUserId = getCurrentUser()?.id ?: return false
        
        return withContext(Dispatchers.IO) {
            try {
                // Delete both directions of the friendship
                supabase.from("user_friends")
                    .delete {
                        filter {
                            eq("user_id", currentUserId)
                            eq("friend_id", friendId)
                        }
                    }
                
                supabase.from("user_friends")
                    .delete {
                        filter {
                            eq("user_id", friendId)
                            eq("friend_id", currentUserId)
                        }
                    }
                
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun searchUsers(query: String): List<UserInformationWithAvatar> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.from("user_info_with_avatar")
                    .select() {
                        filter {
                            ilike("username", "%$query%")
                        }
                        limit(10)
                    }
                    .decodeList<UserInformationWithAvatar>()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private suspend fun getUserIdFromUsername(username: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val result = supabase.from("user_information")
                    .select() {
                        filter {
                            eq("username", username)
                        }
                    }
                    .decodeSingleOrNull<UserInformation>()
                result?.uid
            } catch (e: Exception) {
                println("Error getting user ID from username: ${e.message}")
                null
            }
        }
    }
}