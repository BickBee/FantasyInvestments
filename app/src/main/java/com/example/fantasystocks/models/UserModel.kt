package com.example.fantasystocks.models

import com.example.fantasystocks.classes.User
import com.example.fantasystocks.classes.UserInformationWithAvatar
import com.example.fantasystocks.database.SupabaseClient
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class UserModel () {
    private val supabase = SupabaseClient.supabase

    // used for search bar queries (deprecated see auth table)
    suspend fun getUsersLike(query: String, lim: Long): List<UserInformationWithAvatar> = withContext(Dispatchers.IO) {
        try {
            supabase
                .from("user_info_with_avatar")
                .select() {
                    filter {
                        ilike("username", "%${query}%")
                    }
                    limit(count = lim)
                }
                .decodeList<UserInformationWithAvatar>()
        } catch (e: Exception) {
            println("ERROR FETCHING USERS: $e")
            emptyList<UserInformationWithAvatar>()
        }
    }

    suspend fun getUserInfo(uid: String): UserInformationWithAvatar? {
        return try {
            supabase
                .from("user_info_with_avatar")
                .select {
                    filter {
                        eq("uid", uid)
                    }
                }
                .decodeSingle<UserInformationWithAvatar>()
        } catch (e: Exception) {
            println("ERROR GET USER INFORMATION: $e")
            null
        }
    }

}