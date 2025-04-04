package com.example.fantasystocks.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fantasystocks.database.SupabaseClient
import com.example.fantasystocks.models.Friend
import com.example.fantasystocks.models.UserSettings
import com.example.fantasystocks.ui.theme.ThemeManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName

data class UserProfile(
    val email: String = "",
    val username: String = "",
    val userId: String = ""
)

data class SearchResult(
    val id: String,
    val username: String,
    @SerialName("avatar_id")
    val avatarId: Int
)

class ProfileViewModel : ViewModel() {
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Keep track of ongoing operations that can be cancelled
    private var activeLoadingJobs = mutableListOf<Job>()
    
    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()
    
    private val _incomingFriendRequests = MutableStateFlow<List<Friend>>(emptyList())
    val incomingFriendRequests: StateFlow<List<Friend>> = _incomingFriendRequests.asStateFlow()
    
    private val _outgoingFriendRequests = MutableStateFlow<List<Friend>>(emptyList())
    val outgoingFriendRequests: StateFlow<List<Friend>> = _outgoingFriendRequests.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _userSettings = MutableStateFlow<UserSettings?>(null)
    val userSettings: StateFlow<UserSettings?> = _userSettings.asStateFlow()
    
    private val _isSuccess = MutableStateFlow<Boolean?>(null)
    val isSuccess: StateFlow<Boolean?> = _isSuccess.asStateFlow()

    init {
        loadUserProfile()
        loadFriends()
        loadFriendRequests()
        loadUserSettings()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSuccess.value = false
            try {
                val currentUser = SupabaseClient.getCurrentUser()
                if (currentUser != null) {
                    val username = SupabaseClient.getUsername(currentUser.id)
                    if (username != null) {
                        _userProfile.value = UserProfile(
                            email = currentUser.email ?: "",
                            userId = currentUser.id,
                            username = username
                        )
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _isSuccess.value = null
            _errorMessage.value = null
            
            try {
                // Validate username format
                if (newUsername.length < 3) {
                    _errorMessage.value = "Username must be at least 3 characters long"
                    _isSuccess.value = false
                    return@launch
                }
                
                if (!newUsername.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                    _errorMessage.value = "Username can only contain letters, numbers, and underscores"
                    _isSuccess.value = false
                    return@launch
                }
                
                // Skip update if username is the same
                if (newUsername == _userProfile.value.username) {
                    _isSuccess.value = true
                    return@launch
                }
                
                // Check if username is available
                val isAvailable = SupabaseClient.isUsernameAvailable(newUsername)
                if (!isAvailable) {
                    _errorMessage.value = "Username already taken"
                    _isSuccess.value = false
                    return@launch
                }
                
                // Update username
                val success = SupabaseClient.updateUsername(_userProfile.value.userId, newUsername)
                if (success) {
                    _userProfile.value = _userProfile.value.copy(username = newUsername)
                    _isSuccess.value = true
                } else {
                    _errorMessage.value = "Failed to update username"
                    _isSuccess.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating username: ${e.message}"
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadFriends() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val friends = SupabaseClient.getFriends()
                _friends.value = friends
            } catch (e: Exception) {
                _errorMessage.value = "Error loading friends: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadFriendRequests() {
        val job = viewModelScope.launch {
            _isLoading.value = true
            try {
                val incomingRequests = SupabaseClient.getIncomingFriendRequests()
                val outgoingRequests = SupabaseClient.getOutgoingFriendRequests()
                _incomingFriendRequests.value = incomingRequests
                _outgoingFriendRequests.value = outgoingRequests
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _errorMessage.value = "Error loading friend requests: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
        activeLoadingJobs.add(job)
        job.invokeOnCompletion { activeLoadingJobs.remove(job) }
    }
    
    fun sendFriendRequest(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _isSuccess.value = null
            _errorMessage.value = null
            
            try {
                // Skip if trying to add yourself
                if (username == _userProfile.value.username) {
                    _errorMessage.value = "You cannot add yourself as a friend"
                    _isSuccess.value = false
                    return@launch
                }
                
                // Check if already friends
                val existingFriends = _friends.value
                if (existingFriends.any { it.username == username }) {
                    _errorMessage.value = "You are already friends with this user"
                    _isSuccess.value = false
                    return@launch
                }
                
                // Check if request already pending
                val existingRequests = _outgoingFriendRequests.value
                if (existingRequests.any { it.username == username }) {
                    _errorMessage.value = "Friend request already sent"
                    _isSuccess.value = false
                    return@launch
                }
                
                val success = SupabaseClient.sendFriendRequest(username)
                if (success) {
                    _isSuccess.value = true
                    // Refresh outgoing friend requests list
                    loadFriendRequests()
                } else {
                    _errorMessage.value = "Failed to send friend request"
                    _isSuccess.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error sending friend request: ${e.message}"
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun acceptFriendRequest(friendId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val success = SupabaseClient.respondToFriendRequest(friendId, true)
                if (success) {
                    // Refresh both lists
                    loadFriendRequests()
                    loadFriends()
                } else {
                    _errorMessage.value = "Failed to accept friend request"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error accepting friend request: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun rejectFriendRequest(friendId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val success = SupabaseClient.respondToFriendRequest(friendId, false)
                if (success) {
                    // Refresh friend requests
                    loadFriendRequests()
                } else {
                    _errorMessage.value = "Failed to reject friend request"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error rejecting friend request: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val success = SupabaseClient.removeFriend(friendId)
                if (success) {
                    // Refresh friends list
                    loadFriends()
                } else {
                    _errorMessage.value = "Failed to remove friend"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error removing friend: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun searchUsers(query: String) {
        viewModelScope.launch {
            if (query.length < 3) {
                _searchResults.value = emptyList()
                return@launch
            }
            
            _isLoading.value = true
            
            try {
                val users = SupabaseClient.searchUsers(query)
                // Filter out current user, existing friends, and pending requests
                val filteredUsers = users.filter { user ->
                    user.username != _userProfile.value.username &&
                    !_friends.value.any { it.username == user.username } &&
                    !_outgoingFriendRequests.value.any { it.username == user.username }
                }
                _searchResults.value = filteredUsers.map { 
                    SearchResult(it.uid, it.username, it.avatarId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error searching users: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _errorMessage.value = null
    }
    
    fun loadUserSettings() {
        viewModelScope.launch {
            try {
                val settings = SupabaseClient.getUserSettings()
                _userSettings.value = settings
                
                // Update ThemeManager with user's dark mode preference
                settings?.dark_mode?.let { darkMode ->
                    ThemeManager.setDarkTheme(darkMode)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load user settings: ${e.message}"
            }
        }
    }
    
    fun updateUserSettings(darkMode: Boolean, notificationEnabled: Boolean, avatarId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _isSuccess.value = null
            _errorMessage.value = null
            
            try {
                val currentUser = SupabaseClient.getCurrentUser()
                if (currentUser != null) {
                    // If settings haven't changed, don't make a database call
                    val currentSettings = _userSettings.value
                    if (currentSettings != null && 
                        currentSettings.dark_mode == darkMode && 
                        currentSettings.notification_enabled == notificationEnabled && 
                        currentSettings.avatar_id == avatarId) {
                        _isSuccess.value = true
                        _isLoading.value = false
                        return@launch
                    }
                    
                    // Create updated settings
                    val updatedSettings = UserSettings(
                        uid = currentUser.id,
                        dark_mode = darkMode,
                        notification_enabled = notificationEnabled,
                        avatar_id = avatarId
                    )
                    
                    // Update UI state immediately for responsive user experience
                    _userSettings.value = updatedSettings
                    
                    // Update ThemeManager immediately for app-wide theme changes
                    ThemeManager.setDarkTheme(darkMode)
                    
                    // Then update in database
                    val success = SupabaseClient.updateUserSettings(updatedSettings)
                    
                    if (success) {
                        // Force state update to ensure recomposition in collectors
                        // We create a new object with the same values to ensure state change detection
                        _userSettings.value = updatedSettings.copy()
                        _isSuccess.value = true
                    } else {
                        // Revert to previous settings if failed
                        _userSettings.value = currentSettings
                        // Revert ThemeManager if database update failed
                        currentSettings?.dark_mode?.let { ThemeManager.setDarkTheme(it) }
                        _errorMessage.value = "Failed to update settings"
                        _isSuccess.value = false
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating settings: ${e.message}"
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearErrorsAndSuccess() {
        _errorMessage.value = null
        _isSuccess.value = null
    }

    fun cancelFriendRequest(friendId: String) {
        viewModelScope.launch {
            try {
                val success = SupabaseClient.removeFriend(friendId)
                if (success) {
                    // Refresh the friend requests list
                    loadFriendRequests()
                } else {
                    _errorMessage.value = "Failed to cancel friend request"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error canceling friend request: ${e.message}"
            }
        }
    }
    
    /**
     * Cancels any ongoing loading operations and resets loading state
     * This is useful when navigating away from a screen before data loads
     */
    fun cancelLoading() {
        activeLoadingJobs.forEach { it.cancel() }
        activeLoadingJobs.clear()
        _isLoading.value = false
    }
}