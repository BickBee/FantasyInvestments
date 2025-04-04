package com.example.fantasystocks.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fantasystocks.classes.League
import com.example.fantasystocks.classes.Player
import com.example.fantasystocks.classes.Transaction
import com.example.fantasystocks.database.StockRouter
import com.example.fantasystocks.database.SupabaseClient
import com.example.fantasystocks.models.LeagueModel
import com.example.fantasystocks.models.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
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

fun dateToDay(date: LocalDate): String {
    val month = date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val day = date.dayOfMonth
    val year = date.year

    val suffix = when {
        day in 11..13 -> "th" // Special case for 11th, 12th, 13th
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }

    return "$month $day$suffix $year"
}

class LeagueViewModel: ViewModel() {
    private val model = LeagueModel()

    // -------- Current Player --------
    private val _currentPlayer = MutableStateFlow<Player?>(null)
    val currentPlayer: StateFlow<Player?> = _currentPlayer.asStateFlow()

    fun updatePortfolio(stockPrices: Map<Int, List<Double>>) {
        if (_currentPlayer.value != null) {
            val newPortfolio = _currentPlayer.value!!.portfolio.map { (stock, quantity) ->
                val newPrice = stockPrices[stock.id]?.first() ?: stock.price
                Pair(stock.copy(price = newPrice), quantity)
            }
            _currentPlayer.value = _currentPlayer.value!!.copy(portfolio = newPortfolio.toMutableStateMap())
        }
    }
    fun setCurrentPlayer(player: Player) {
        _currentPlayer.value = player
    }
    // -------- END Current Player --------

    // -------- League --------
    private val _league = MutableStateFlow<League?>(null)
    val league: StateFlow<League?> = _league.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(listOf())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    fun fetchLeague(leagueId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val fetchedLeague = model.fetchLeague(leagueId)
                _league.value = fetchedLeague
                val players = model.fetchPlayersWithPortfolios(leagueId)
                players?.let { _league.value?.setPlayers(players.toMutableList()) }
                players?.let { _players.value = players }
                _currentPlayer.value = _league.value?.getCurrentPlayer(SupabaseClient.getCurrentUID()!!)
            } catch (e: Exception) {
                println("ERROR FETCHING LEAGUE")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePortfolios(stockPrices: Map<Int, List<Double>>) {
        _league.value?.let { league ->
            val players = league.getPlayers().map { player ->
                player.copy(
                    portfolio = player.portfolio.map { (stock, quantity) ->
                        stock.copy(
                            price = stockPrices[stock.id]?.firstOrNull() ?: stock.price
                        ) to quantity
                    }.toMutableStateMap()
                )
            }.toMutableList()
            val updatedLeague = _league.value?.copy()
            updatedLeague?.setPlayers(players.toMutableList())
            _league.value = updatedLeague
            _players.value = players
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

    // -------- Historical Values --------
    private val _historicalValues = MutableStateFlow<MutableList<Double>?>(null)
    val historicalValues: StateFlow<MutableList<Double>?> = _historicalValues.asStateFlow()

    private val _historicalLoading = MutableStateFlow(false)
    val historicalLoading: StateFlow<Boolean> = _historicalLoading.asStateFlow()

    fun getHistoricalValues(uid: String, leagueId: Int, initValue: Double) {
        viewModelScope.launch {
            try {
                _historicalLoading.value = true
                val values = model.getHistoricalValues(uid, leagueId).map { it.value }.toMutableList()
                val initValues = listOf(initValue, initValue)
                val combinedValues = (initValues + values).toMutableList()
                _historicalValues.value = combinedValues
            } catch (e: Exception) {
                println("ERROR GETTING HISTORICAL VALUES: $e")
            } finally {
                _historicalLoading.value = false
            }
        }
    }

    fun updateHistorical(value: Double) {
        _historicalValues.value?.add(value)
    }
    // -------- END Historical Values --------
}