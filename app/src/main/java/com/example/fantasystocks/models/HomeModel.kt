package com.example.fantasystocks.models

import com.example.fantasystocks.classes.League
import com.example.fantasystocks.classes.User
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeModel() {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://ogeuugctypdhscburggm.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9nZXV1Z2N0eXBkaHNjYnVyZ2dtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDAwMDIzNTAsImV4cCI6MjA1NTU3ODM1MH0.DIOHx3_S4Eq9jbvvHe0Pvr6J-24MpL8clDEdITkghpI"
    ) {
        install(Postgrest)
    }

    // used for search bar queries
     suspend fun getUsersLike(query: String, lim: Long): List<User> = withContext(Dispatchers.IO) {
         try {
             supabase
                 .from("users")
                 .select() {
                     filter {
                         ilike("username", "%${query}%")
                    }
                     limit(count = lim)
                 }
                 .decodeList<User>()
         } catch (e: Exception) {
             println("ERROR FETCHING USERS: ${e}")
             emptyList<User>()
         }
    }

    suspend fun getLeagues(): List<League> = withContext(Dispatchers.IO) {
        try {
            supabase
                .from("leagues")
                .select()
                .decodeList<League>()
        } catch (e: Exception) {
            println("ERROR: $e")
            emptyList<League>()
        }
    }

    // insert new league
    suspend fun insertLeague(league: League) {
        try {
            supabase
                .from("leagues")
                .insert(league)
        } catch (e: Exception) {
            println("ERROR INSERTING LEAGUE: $e")
        }
    }

    // insert new league and return the entry
    suspend fun insertLeagueAndReturn(league: League): League? {
        return try {
            supabase
                .from("leagues")
                .insert(league) {
                    select()
                }
                .decodeSingle<League>()
        } catch (e: Exception) {
            println("ERROR INSERTING LEAGUE: $e")
            null
        }
    }
}
