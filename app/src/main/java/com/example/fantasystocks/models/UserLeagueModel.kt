package com.example.fantasystocks.models

import com.example.fantasystocks.classes.UserLeague
import com.example.fantasystocks.database.SupabaseClient
import io.github.jan.supabase.postgrest.from

class UserLeagueModel() {
    private val supabase = SupabaseClient.supabase

    suspend fun getUsersLeagues(uid: String): List<UserLeague> {
        return try {
            supabase
                .from("user_league")
                .select() {
                    filter {
                        eq("uid", uid)
                    }
                }
                .decodeList<UserLeague>()
        } catch (e: Exception) {
            println("ERROR FETCHING USER_LEAGUES: $e")
            listOf()
        }
    }

    suspend fun insertUserLeague(userLeague: UserLeague) {
        try {
            supabase
                .from("user_league")
                .insert(userLeague)
        } catch (e: Exception) {
            println("ERROR INSERTING USER_LEAGUE: $e")
        }
    }

    suspend fun insertUserLeagueList(users: List<UserLeague>) {
        try {
            supabase
                .from("user_league")
                .insert(users)
        } catch (e: Exception) {
            println("ERROR INSERTING LIST OF USER_LEAGUES: $e")
        }
    }
}
