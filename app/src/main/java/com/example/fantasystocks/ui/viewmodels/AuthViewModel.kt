package com.example.fantasystocks.ui.viewmodels

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fantasystocks.database.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel() : ViewModel() {
    private val _isAuthenticated = MutableStateFlow<Boolean?>(null)
    private val supabaseClient = SupabaseClient()
    val isAuthenticated: StateFlow<Boolean?> = _isAuthenticated.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                val session = supabaseClient.supabase.auth.currentSessionOrNull()
                _isAuthenticated.value = session != null
                println("Current session: ${session?.user?.email}")
            } catch (e: Exception) {
                println("Error checking auth state: ${e.message}")
                _isAuthenticated.value = false
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                println("Attempting to sign in with email: $email")
                try {
                    supabaseClient.signInExistingUser(email, password)
                    println("Sign in successful")
                    _isAuthenticated.value = true
                    _errorMessage.value = null
                } catch (e: io.github.jan.supabase.exceptions.RestException) {
                    println("Detailed error: ${e.toString()}")
                    println("Error response: ${e.response}")
                    _errorMessage.value = "Sign in failed: ${e.toString()}"
                    _isAuthenticated.value = false
                }
            } catch (e: Exception) {
                println("Sign in failed with exception: ${e::class.simpleName}")
                println("Error details: ${e.toString()}")
                println("Stack trace: ${e.stackTraceToString()}")
                _errorMessage.value = "Sign in failed: ${e.toString()}"
                _isAuthenticated.value = false
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                // Validate password
                if (password.length < 6) {
                    _errorMessage.value = "Password must be at least 6 characters long"
                    return@launch
                }

                println("Attempting to sign up with email: $email")
                println("Password length: ${password.length}")
                
                try {
                    supabaseClient.signUpNewUser(email, password)
                    println("Sign up successful")
                    _isAuthenticated.value = true
                    _errorMessage.value = null
                } catch (e: io.github.jan.supabase.exceptions.RestException) {
                    println("Detailed error: ${e.toString()}")
                    println("Error response: ${e.response}")
                    _errorMessage.value = "Sign up failed: ${e.toString()}"
                    _isAuthenticated.value = false
                }
            } catch (e: Exception) {
                println("Sign up failed with exception: ${e::class.simpleName}")
                println("Error details: ${e.toString()}")
                println("Stack trace: ${e.stackTraceToString()}")
                _errorMessage.value = "Sign up failed: ${e.toString()}"
                _isAuthenticated.value = false
            }
        }
    }
}