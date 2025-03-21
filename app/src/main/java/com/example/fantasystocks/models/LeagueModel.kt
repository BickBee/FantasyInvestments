package com.example.fantasystocks.models

import com.example.fantasystocks.classes.League
import com.example.fantasystocks.classes.Player
import com.example.fantasystocks.classes.PlayerPortfolioView
import com.example.fantasystocks.classes.Stock
import com.example.fantasystocks.classes.Transaction
import com.example.fantasystocks.classes.UserLeague
import com.example.fantasystocks.database.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

enum class DateType {
    START,
    END
}

class LeagueModel() {
    private val supabase = SupabaseClient.supabase

    suspend fun getLeagues(leagueIds : List<Int>): List<League> = withContext(Dispatchers.IO) {
        try {
            supabase
                .from("leagues")
                .select() {
                    filter {
                        isIn("league_id", leagueIds)
                    }
                }
                .decodeList<League>()
        } catch (e: Exception) {
            println("ERROR: $e")
            emptyList<League>()
        }
    }

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
    suspend fun insertLeagueAndReturnId(league: League): Int? {
        return try {
            supabase
                .from("leagues")
                .insert(league) {
                    select()
                }
                .decodeSingle<League>()
                .id
        } catch (e: Exception) {
            println("ERROR INSERTING LEAGUE: $e")
            null
        }
    }

    suspend fun fetchLeague(leagueId: Int): League? {
        return try {
            supabase
                .from("leagues")
                .select {
                    filter {
                        eq("league_id", leagueId)
                    }
                }
                .decodeSingle<League>()
        } catch (e: Exception) {
            println("ERROR FETCHING LEAGUE: $e")
            null
        }
    }

    suspend fun fetchPlayers(leagueId: Int): List<UserLeague>? {
        return try {
            supabase
                .from("user_league")
                .select {
                    filter {
                        eq("league_id", leagueId)
                    }
                }
                .decodeList<UserLeague>()
        } catch (e: Exception) {
            println(e)
            null
        }
    }

    suspend fun fetchPlayersWithPortfolios(leagueId: Int): List<Player>? {
        return try {
            val result = supabase
                .from("player_portfolio_view")
                .select {
                    filter {
                        eq("league_id", leagueId)
                    }
                }
                .decodeList<PlayerPortfolioView>()
            result.groupBy { it.id }
                .map { (id, entries) ->
                    val firstEntry = entries.first()
                    val portfolio = mutableMapOf<Stock, Int>()
                    entries.forEach { entry ->
                        if (entry.stockId != null && entry.stockName != null &&
                            entry.stockTicker != null && entry.quantity != null) {
                            portfolio[Stock(
                                id = entry.stockId,
                                name = entry.stockName,
                                ticker = entry.stockTicker)
                            ] = entry.quantity.toInt()
                        }
                    }
                    Player(
                        name = firstEntry.username,
                        id = firstEntry.id,
                        initValue = firstEntry.initValue,
                        cash = firstEntry.cash,
                        portfolio = portfolio
                    )
            }
        } catch (e: Exception) {
            println("ERROR FETCHING PLAYERS WITH PORTFOLIOS: $e")
            null
        }
    }

    suspend fun updateLeagueName(newName: String, leagueId: Int) {
        try {
            supabase
                .from("leagues")
                .update(
                    { set("name", newName) }
                ) {
                    filter {
                        eq("league_id", leagueId)
                    }
                }
        } catch (e: Exception) {
            println("ERROR UPDATING LEAGUE NAME: $e")
        }
    }

    suspend fun removePlayer(playerId: String, leagueId: Int) {
        try {
            suspend fun deleteFrom(tableName: String) {
                supabase
                    .from(tableName)
                    .delete {
                        filter {
                            eq("league_id", leagueId)
                            eq("uid", playerId)
                        }
                    }
            }
            deleteFrom("user_league")
//            deleteFrom("historical_portfolio_value")
//            deleteFrom("portfolio")
//             deleteFrom("transactions")
        } catch (e: Exception) {
            println("ERROR REMOVING PLAYER: $e")
        }
    }

    suspend fun updateDate(leagueId: Int, newDate: LocalDate, dateType: DateType) {
        val dateCol = if (dateType == DateType.START) "start_date" else "end_date"
        try {
            supabase
                .from("leagues")
                .update(
                    {
                        set(dateCol, newDate)
                    }
                ) {
                    filter {
                        eq("league_id", leagueId)
                    }
                }
        } catch (e: Exception) {
            println("ERROR UPDATING START DATE: $e")
        }
    }

    suspend fun getUserTxns(uid: String, leagueId: Int): List<Transaction>? {
        return try {
            supabase
                .from("transaction_view")
                .select {
                    filter {
                        eq("uid", uid)
                        eq("league_id", leagueId)
                    }
                    order(column = "timestamp", order = Order.DESCENDING)
                }
                .decodeList<Transaction>()
        } catch (e: Exception) {
            println("ERROR FETCHING USERS TRANSACTIONS: $e")
            null
        }
    }
}