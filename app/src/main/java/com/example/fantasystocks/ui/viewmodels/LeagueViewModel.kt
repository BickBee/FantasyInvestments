package com.example.fantasystocks.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fantasystocks.classes.Leaderboard
import com.example.fantasystocks.classes.League
import com.example.fantasystocks.classes.Player
import com.example.fantasystocks.classes.Stock
import com.example.fantasystocks.classes.Transaction
import com.example.fantasystocks.models.LeagueModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

fun doubleMoneyToString(money: Double, space: Boolean = false): String {
    val stringMoney =  String.format(Locale.US, "%.2f", money)
    val (intMoney, decMoney) = stringMoney.split(".")
    val intStr = doubleStringToMoneyString(intMoney)
    return if (space) "$ $intStr.$decMoney" else "$$intStr.$decMoney"
}

fun timestampToDay(timestamp: String): String {
    val dateTime = LocalDateTime.parse(timestamp)
    val month = dateTime.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val day = dateTime.dayOfMonth
    val year = dateTime.year

    val suffix = when (day % 10) {
        1 -> if (day == 11) "th" else "st"
        2 -> if (day == 12) "th" else "nd"
        3 -> if (day == 13) "th" else "rd"
        else -> "th"
    }
    return "$month $day$suffix $year"
}

class LeagueViewModel: ViewModel() {
    private val model = LeagueModel()

    // -------- League --------
    private val _league = MutableStateFlow<League?>(null)
    val league: StateFlow<League?> = _league

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    fun fetchLeague(leagueId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val fetchedLeague = model.fetchLeague(leagueId)
                _league.value = fetchedLeague
                val players = model.fetchPlayersWithPortfolios(leagueId)
                players?.let { _league.value?.setPlayers(players.toMutableList()) }
            } catch (e: Exception) {
                println("ERROR FETCHING LEAGUE")
            } finally {
                _isLoading.value = false
            }
        }
    }
    // -------- END League --------

    // -------- Tab --------
    var selectedTab by mutableStateOf(0)
        private set
    fun selectPersonal() { selectedTab = 0 }
    fun selectShared() { selectedTab = 1 }
    // -------- END Tab --------

    // -------- Personal Performance --------
    var personalPerformanceTab by mutableStateOf(0)
        private set
    fun selectPortfolio() { personalPerformanceTab = 0 }
    fun selectActivity() { personalPerformanceTab = 1 }
    // -------- END Personal Performance --------

    // -------- Leaderboard --------
    var leaderboardExpanded by mutableStateOf(false)
        private set
    fun clickLeaderboardExpanded() { leaderboardExpanded = !leaderboardExpanded }
    fun closeLeaderboardExpanded() { leaderboardExpanded = false }
    // -------- END Leaderboard --------



    // -------- Transactions --------
    private val _transactions = MutableStateFlow<List<Transaction>?>(null)
    val transactions: StateFlow<List<Transaction>?> = _transactions.asStateFlow()

    private val _txnLoading = MutableStateFlow(false)
    val txnLoading: StateFlow<Boolean> = _txnLoading.asStateFlow()

    fun getTxns(uid: String, leagueId: Int) {
        viewModelScope.launch {
            try {
                _txnLoading.value = true
                val txns = model.getUserTxns(uid, leagueId)
                println(txns)
                _transactions.value = txns
            } catch (e: Exception) {
                println("ERROR GETTING TXNS: $e")
                _transactions.value = null
            } finally {
                _txnLoading.value = false
            }
        }
    }
    // -------- END Transactions --------

    // portfolio chart values
    fun portValuesInit(): List<Double> {
        return listOf(4095.77, 4034.29, 3958.20, 3723.18, 3689.83, 3773.59)
    }
}