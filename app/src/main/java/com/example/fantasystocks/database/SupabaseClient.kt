package com.example.fantasystocks.database

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable

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

    fun load() {
        var temp: List<Instrument>? = null
        runBlocking {
            withContext(Dispatchers.IO) {
                temp = supabase.from("instruments").select().decodeList<Instrument>()
            }
        }
        println(temp)
    }

    // Just a skeleton function, need to decodeList after select if you want it to be displayed
    fun queryTable(tableName: String) {
        var temp: PostgrestResult? = null
        runBlocking {
            withContext(Dispatchers.IO) {
                temp = supabase.from(tableName).select()
            }
        }
        println(temp)
    }

    suspend fun signUpNewUser(emailInput: String, passwordInput: String) {
        withContext(Dispatchers.IO) {
            supabase.auth.signUpWith(Email) {
                email = emailInput
                password = passwordInput
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
}

@Serializable
data class Instrument(
    val id: Int,
    val name: String,
)