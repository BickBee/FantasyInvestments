package com.example.fantasystocks.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fantasystocks.database.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserProfile(
    val email: String = "",
    val name: String = "User",
    val userId: String = ""
)

class ProfileViewModel : ViewModel() {
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = SupabaseClient.getCurrentUser()
                if (currentUser != null) {
                    _userProfile.value = UserProfile(
                        email = currentUser.email ?: "",
                        userId = currentUser.id,
                        name = currentUser.userMetadata?.get("name") as? String ?: "User"
                    )
                }
            } catch (e: Exception) {
                // Handle the error, possibly log it
            } finally {
                _isLoading.value = false
            }
        }
    }
}