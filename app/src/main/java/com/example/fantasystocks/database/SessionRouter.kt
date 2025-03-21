package com.example.fantasystocks.database

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserLeagueData(
    val uid: String,
    val league_id: Int,
    val cash: Double,
)

@Serializable
data class LeagueData(
    @SerialName("league_id")
    val leagueId: Int,
    var name: String,
    @SerialName("start_date")
    var startDate: LocalDate? = null,
    @SerialName("end_date")
    var endDate: LocalDate? = null)

object SessionRouter {
    private val databaseConnector = SupabaseClient
    private const val LEAGUE_TABLE_NAME = "leagues"
    private const val USER_LEAGUE_TABLE = "user_league"

    suspend fun getLeagues(): List<LeagueData> = withContext(Dispatchers.IO) {
        try {
            databaseConnector.supabase
                .from(LEAGUE_TABLE_NAME)
                .select()
                .decodeList<LeagueData>()
        } catch (e: Exception) {
            println("ERROR: $e")
            emptyList<LeagueData>()
        }
    }

    suspend fun getUserLeagues(userId: String): List<LeagueData> = withContext(Dispatchers.IO) {
        try {
            Log.d("SessionRouter", "getUserLeagues: Starting for userId: $userId")

            val columns = Columns.raw(
                """
            league_id,
            name, 
            start_date,
            end_date,
            user_league!inner (league_id, uid)
            """.trimIndent()
            )
            Log.d("SessionRouter", "getUserLeagues: Using columns: $columns")

            val query = databaseConnector.supabase
                .from(LEAGUE_TABLE_NAME)
                .select(columns = columns) {
                    filter {
                        eq("user_league.uid", userId)
                    }
                }
            Log.d("SessionRouter", "getUserLeagues: Query built: $query")

            val leagues = query.decodeList<LeagueData>()
            Log.d("SessionRouter", "getUserLeagues: Retrieved ${leagues.size} leagues")
            leagues
        } catch (e: Exception) {
            println("ERROR: $e")
            emptyList<LeagueData>()
        }
    }

    suspend fun getSessionBalance(leagueId: Int, uid: String): Double {
        try {
            val sessionObject = databaseConnector.supabase.from(USER_LEAGUE_TABLE).select() {
                filter {
                    eq("uid", uid)
                    eq("league_id", leagueId)
                }
            }.decodeSingleOrNull<UserLeagueData>()

            if (sessionObject != null) {
                return sessionObject.cash
            } else {
                throw Exception("Session not found")
            }


        } catch (e: Exception) {
            println("ERROR: $e")
            // should never reach here
            throw e
        }
    }

    suspend fun updateSessionBalance(uid: String, leagueId: Int, newCash: Double) {
        databaseConnector.supabase.from(USER_LEAGUE_TABLE)
            .update({
                set("cash", newCash)
            }) {
                filter {
                    eq("league_id", leagueId)
                    eq("uid", uid)
                }
            }
    }

}