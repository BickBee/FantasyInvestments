package com.example.fantasystocks.models

import com.example.fantasystocks.classes.User
import com.example.fantasystocks.database.SupabaseClient
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserModel () {
    private val supabase = SupabaseClient.supabase

    // used for search bar queries (deprecated see auth table)
    suspend fun getUsersLike(query: String, lim: Long): List<UserInformation> = withContext(Dispatchers.IO) {
        try {
            supabase
                .from("user_information")
                .select() {
                    filter {
                        ilike("username", "%${query}%")
                    }
                    limit(count = lim)
                }
                .decodeList<UserInformation>()
        } catch (e: Exception) {
            println("ERROR FETCHING USERS: $e")
            emptyList<UserInformation>()
        }
    }

    suspend fun getUserInfo(uid: String): UserInformation? {
        return try {
            supabase
                .from("user_information")
                .select {
                    filter {
                        eq("uid", uid)
                    }
                }
                .decodeSingle<UserInformation>()
        } catch (e: Exception) {
            println("ERROR GET USER INFORMATION: $e")
            null
        }
    }

}