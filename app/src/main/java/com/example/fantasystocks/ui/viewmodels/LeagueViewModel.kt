package com.example.fantasystocks.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.fantasystocks.classes.Leaderboard
import com.example.fantasystocks.classes.League
import com.example.fantasystocks.classes.Player
import com.example.fantasystocks.classes.Stock
import com.example.fantasystocks.classes.Transaction
import com.example.fantasystocks.classes.TxnType
import kotlinx.datetime.LocalDate
import java.util.Locale

fun doubleMoneyToString(money: Double, space: Boolean = false): String {
    val stringMoney =  String.format(Locale.US, "%.2f", money)
    val (intMoney, decMoney) = stringMoney.split(".")
    val intStr = doubleStringToMoneyString(intMoney)
    return if (space) "$ $intStr.$decMoney" else "$$intStr.$decMoney"
}

class LeagueViewModel: ViewModel() {

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

    // init before db
    fun init(league: League): Leaderboard {
        if (league.getPlayers().size > 4) return Leaderboard(league)
        val stock1 = Stock("Apple", "APPL", 235.26)
        val stock2 = Stock("Boeing", "BA", 30.82)
        val stock3 = Stock("General Motors", "GM", 400.41)
        val stock4 = Stock("NVIDIA", "NVDA", 130.59)
        val p1 = Player("Dave", 1192, 5000.0, 1187.13,
            mutableMapOf(Pair(stock1, 2), Pair(stock2, 15), Pair(stock3, 2), Pair(stock4, 5))
        )
        val p2 = Player("Charlie", 129, 10000.0, 2500.0,
            mutableMapOf(Pair(stock1, 2), Pair(stock2, 5), Pair(stock3, 10))
        )
        val p3 = Player("Bob", 118, 1000.0, 400.57,
            mutableMapOf(Pair(stock1, 5))
        )
        val p4 = Player("Alice", 103, 1000.0, 115.0,
            mutableMapOf(Pair(stock1, 3), Pair(stock2, 2))
        )
        val p5 = Player("Derrick", 130, 12000.0, 600.0,
            mutableMapOf(Pair(stock1, 2), Pair(stock2, 5), Pair(stock3, 10))
        )
        val p6 = Player("Eric", 159, 10000.0, 2500.0,
            mutableMapOf(Pair(stock1, 1), Pair(stock2, 6), Pair(stock3, 4))
        )
        val p7 = Player("George", 160, 8000.0, 700.0,
            mutableMapOf(Pair(stock1, 1), Pair(stock2, 3), Pair(stock3, 12))
        )
        val p8 = Player("Henry", 162, 8000.0, 1200.0,
            mutableMapOf(Pair(stock1, 1), Pair(stock2, 1), Pair(stock3, 4))
        )
        val p9 = Player("You", 192, 5000.0, 1387.00,
            mutableMapOf(Pair(stock1, 2), Pair(stock2, 15), Pair(stock3, 2), Pair(stock4, 5))
        )
        league.addPlayer(p1)
        league.addPlayer(p2)
        league.addPlayer(p3)
        league.addPlayer(p4)
        league.addPlayer(p5)
        league.addPlayer(p6)
        league.addPlayer(p7)
        league.addPlayer(p8)
        league.addPlayer(p9)
        return Leaderboard(league)
    }
    // port init
    fun portInit(): Player {
        val stock1 = Stock("Apple", "APPL", 235.26)
        val stock2 = Stock("Boeing", "BA", 30.82)
        val stock3 = Stock("General Motors", "GM", 400.41)
        val stock4 = Stock("NVIDIA", "NVDA", 130.59)
        val player = Player("You", 192, 5000.0, 1387.00,
            mutableMapOf(Pair(stock1, 2), Pair(stock2, 15), Pair(stock3, 2), Pair(stock4, 5))
        )
        return player
    }

    // txn activity init
    fun activityInit(): List<Transaction> {
        val txn1 = Transaction(
            type = TxnType.BUY,
            stock = "NVDA",
            amount = 5.00,
            date = LocalDate(2025, 1, 28)
        )
        val txn2 = Transaction(
            type = TxnType.BUY,
            stock = "APPL",
            amount = 5.00,
            date = LocalDate(2025, 1, 29)
        )
        val txn3 = Transaction(
            type = TxnType.BUY,
            stock = "BA",
            amount = 5.00,
            date = LocalDate(2025, 1, 29)
        )
        val txn4 = Transaction(
            type = TxnType.SELL,
            stock = "APPL",
            amount = 3.00,
            date = LocalDate(2025, 2, 10)
        )
        val txn5 = Transaction(
            type = TxnType.BUY,
            stock = "BA",
            amount = 10.00,
            date = LocalDate(2025, 2, 12)
        )
        val txn6 = Transaction(
            type = TxnType.BUY,
            stock = "GM",
            amount = 2.00,
            date = LocalDate(2025, 2, 15)
        )
        return listOf(txn6, txn5, txn4, txn3, txn2, txn1)
    }

    // portfolio chart values
    fun portValuesInit(): List<Double> {
        return listOf(4095.77, 4034.29, 3958.20, 3723.18, 3689.83, 3773.59)
    }
}