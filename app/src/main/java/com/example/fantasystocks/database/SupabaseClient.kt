package com.example.fantasystocks.database

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable

class SupabaseClient {
        val supabase = createSupabaseClient(
        supabaseUrl = "https://lnfecoxuwybrlhzjqxkb.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxuZmVjb3h1d3licmxoempxeGtiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzg5NDQwMTMsImV4cCI6MjA1NDUyMDAxM30.wCjoCRqMTLOWyPxX-9lMohKbxESbP8z6G0FM2Gk3GLY"
        ) {
        install(Postgrest)
            install(Auth)
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
                temp = supabase.from("instruments").select()
            }
        }
        println(temp)
    }

    fun signUpNewUser(emailInput: String, passwordInput: String) {
        runBlocking {
            supabase.auth.signUpWith(Email) {
                email = emailInput
                password = passwordInput
            }
        }
    }

    fun signInExistingUser(emailInput: String, passwordInput: String) {
        runBlocking {
            supabase.auth.signInWith(Email) {
                email = emailInput
                password = passwordInput
            }
        }
    }
}

@Serializable
data class Instrument(
    val id: Int,
    val name: String,
)