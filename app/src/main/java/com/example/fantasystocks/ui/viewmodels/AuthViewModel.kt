package com.example.fantasystocks.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fantasystocks.database.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val TAG = "AuthViewModel"

    private val _isAuthenticated = MutableStateFlow<Boolean?>(null)
    val isAuthenticated: StateFlow<Boolean?> = _isAuthenticated.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isPasswordResetSent = MutableStateFlow(false)
    val isPasswordResetSent: StateFlow<Boolean> = _isPasswordResetSent.asStateFlow()

    private val _isPasswordChanged = MutableStateFlow(false)
    val isPasswordChanged: StateFlow<Boolean> = _isPasswordChanged.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUsernameCheckLoading = MutableStateFlow(false)
    val isUsernameCheckLoading: StateFlow<Boolean> = _isUsernameCheckLoading.asStateFlow()

    private val _isUsernameAvailable = MutableStateFlow<Boolean?>(null)
    val isUsernameAvailable: StateFlow<Boolean?> = _isUsernameAvailable.asStateFlow()

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        viewModelScope.launch {
            try {
                _isAuthenticated.value = SupabaseClient.isAuthenticated()

                val currentUser = SupabaseClient.getCurrentUser()
                if (currentUser != null) {
                    Log.d(TAG, "Current session found for user: ${currentUser.email}")
                } else {
                    Log.d(TAG, "No active session found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking auth state: ${e.message}", e)
                _isAuthenticated.value = false
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                Log.d(TAG, "Attempting to sign in with email: $email")

                try {
                    SupabaseClient.signInExistingUser(email, password)
                    // Check auth state to confirm success
                    delay(500) // Small delay to allow session to be established
                    checkAuthState()
                    Log.d(TAG, "Sign in successful for: $email")
                } catch (e: RestException) {
                    Log.e(TAG, "Sign in failed with REST error: ${e.message}", e)
                    _errorMessage.value = "Invalid credentials"
                    _isAuthenticated.value = false
                    _isLoading.value = false
                } catch (e: Exception) {
                    Log.e(TAG, "Sign in failed with exception: ${e.message}", e)
                    _errorMessage.value = "Sign in failed"
                    _isAuthenticated.value = false
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during sign in: ${e.message}", e)
                Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                _errorMessage.value = "Sign in failed"
                _isAuthenticated.value = false
                _isLoading.value = false
            } finally {
                if (_isAuthenticated.value == true) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun checkUsernameAvailability(username: String) {
        viewModelScope.launch {
            try {
                _isUsernameCheckLoading.value = true
                _errorMessage.value = null
                
                val isAvailable = SupabaseClient.isUsernameAvailable(username)
                _isUsernameAvailable.value = isAvailable
                
                if (!isAvailable) {
                    _errorMessage.value = "Username already taken"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking username availability: ${e.message}", e)
                _errorMessage.value = "Failed to check username availability"
                _isUsernameAvailable.value = false
            } finally {
                _isUsernameCheckLoading.value = false
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, username: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Validate password
                if (password.length < 6) {
                    Log.w(TAG, "Password too short during signup attempt")
                    _errorMessage.value = "Password must be at least 6 characters long"
                    _isLoading.value = false
                    return@launch
                }

                Log.d(TAG, "Attempting to sign up with email: $email and username: $username")
                
                try {
                    SupabaseClient.signUpNewUser(email, password, username)
                    // Check auth state after sign up
                    delay(500) // Small delay to allow session to be established
                    checkAuthState()
                    Log.d(TAG, "Sign up successful for: $email")
                } catch (e: RestException) {
                    Log.e(TAG, "Sign up failed with REST error: ${e.message}", e)
                    _errorMessage.value = "Account already exists"
                    _isAuthenticated.value = false
                    _isLoading.value = false
                } catch (e: Exception) {
                    Log.e(TAG, "Sign up failed with exception: ${e.message}", e)
                    _errorMessage.value = "Sign up failed"
                    _isAuthenticated.value = false
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during sign up: ${e.message}", e)
                Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                _errorMessage.value = "Sign up failed"
                _isAuthenticated.value = false
                _isLoading.value = false
            } finally {
                if (_isAuthenticated.value == true) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                Log.d(TAG, "Attempting to reset password for: $email")
                
                SupabaseClient.resetPasswordForEmail(email)
                _isPasswordResetSent.value = true
                Log.d(TAG, "Password reset email sent to: $email")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send password reset: ${e.message}", e)
                _errorMessage.value = "Failed to send password reset email"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                if (newPassword.length < 6) {
                    _errorMessage.value = "Password must be at least 6 characters long"
                    _isLoading.value = false
                    return@launch
                }
                
                Log.d(TAG, "Attempting to change password")

                try {
                    // First verify current password by attempting to re-authenticate
                    val currentUser = SupabaseClient.getCurrentUser()
                    if (currentUser != null) {
                        val email = currentUser.email
                        if (email != null) {
                            try {
                                // Re-authenticate with current credentials
                                SupabaseClient.signInExistingUser(email, currentPassword)
                                // If successful, update password
                                SupabaseClient.updatePassword(newPassword)
                                _isPasswordChanged.value = true
                                Log.d(TAG, "Password changed successfully for: $email")
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to verify current password: ${e.message}", e)
                                _errorMessage.value = "Current password is incorrect"
                            }
                        } else {
                            Log.e(TAG, "Unable to verify user email - email is null")
                            _errorMessage.value = "Unable to verify user email"
                        }
                    } else {
                        Log.e(TAG, "No authenticated user found")
                        _errorMessage.value = "No authenticated user found"
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to change password: ${e.message}", e)
                    _errorMessage.value = "Failed to change password"
                }

                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during password change: ${e.message}", e)
                _errorMessage.value = "Failed to change password"
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Attempting to sign out")
                SupabaseClient.signOut()
                _isAuthenticated.value = false
                Log.d(TAG, "Sign out successful")
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sign out: ${e.message}", e)
                _errorMessage.value = "Failed to sign out"
                _isLoading.value = false
            }
        }
    }

    fun clearErrors() {
        _errorMessage.value = null
        _isUsernameAvailable.value = null
    }

    fun resetState() {
        _isPasswordResetSent.value = false
        _isPasswordChanged.value = false
        _isUsernameAvailable.value = null
    }
}