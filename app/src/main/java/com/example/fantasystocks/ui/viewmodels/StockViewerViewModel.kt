package com.example.fantasystocks.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fantasystocks.DATA_FETCHING_DELAY_MS
import com.example.fantasystocks.TRANSACTION_FEE
import com.example.fantasystocks.database.LeagueData
import com.example.fantasystocks.database.SessionRouter
import com.example.fantasystocks.database.StockRouter
import com.example.fantasystocks.database.SupabaseClient.getCurrentUID
import com.example.fantasystocks.database.Transaction
import com.example.fantasystocks.database.TransactionRouter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

val NO_SESSION_SELECTED = "Select a session"

data class StockViewerState(
    val currentUserId: String = "",
    val currentLeagueId: Int = -100,
    val currentStockId: Int = -1,
    var selectedSession: String = NO_SESSION_SELECTED,
    val quantity: String = "",
    val isSessionDropdownExpanded: Boolean = false,
    val balance: Double = 0.0,
    val stockBalance: Double = 0.0,
    val stockData: List<Double> = listOf(),
    var sessions: List<LeagueData> = listOf(),
    val message: String? = null,
    val stockTicker: String = "",
    val stockOpen: Double = 0.0,
    val stockClose: Double = 0.0,
    val stockHigh: Double = 0.0,
    val stockLow: Double = 0.0,
    val latestPrice: Double = 0.0
)

class StockViewerViewModel(private val stockTicker: String) : ViewModel() {
    private val _state = MutableStateFlow(StockViewerState())
    val state: StateFlow<StockViewerState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val currentUserId = getCurrentUID()
            if (currentUserId != null) {
                _state.value = _state.value.copy(currentUserId = currentUserId)
            }
            Log.d("StockViewerViewModel", "Current user ID: ${state.value.currentUserId}")
        }
        startFetchStockData()
        getLeagues()
    }

    fun updateQuantity(newQuantity: String) {
        if (newQuantity.all { it.isDigit() } || newQuantity.isEmpty()) {
            _state.value = _state.value.copy(quantity = newQuantity)
        }
    }

    fun updateSessionDropdownExpanded(expanded: Boolean) {
        _state.value = _state.value.copy(isSessionDropdownExpanded = expanded)
    }

    fun updateSelectedSession(session: LeagueData) {
        viewModelScope.launch {
            try {
                Log.d("StockViewerViewModel", "Updating selected session to ${session.name}")
                // Use the new league id from the session
                val balanceForSession = SessionRouter.getSessionBalance(session.leagueId, state.value.currentUserId)
                val stockBalance = StockRouter.getStockQuantity(session.leagueId, state.value.currentUserId, state.value.currentStockId)

                _state.value = _state.value.copy(
                    selectedSession = session.name,
                    balance = balanceForSession,
                    currentLeagueId = session.leagueId,
                    stockBalance = stockBalance,
                    isSessionDropdownExpanded = false,
                    message = "Session updated successfully"
                )
                Log.d("StockViewerViewModel", "Updated session to ${session.name}")
            } catch (e: Exception) {
                Log.e("StockViewerViewModel", "Failed to update session", e)
                _state.value = _state.value.copy(
                    message = "Failed to update session: ${e.message}"
                )
            }
        }
    }


    fun createTransaction(isBuy: Boolean) {
        viewModelScope.launch {
            if(state.value.selectedSession == NO_SESSION_SELECTED) {
                _state.value = _state.value.copy(message = "No session selected")
                return@launch
            }
            if(state.value.quantity.isEmpty()) {
                _state.value = _state.value.copy(message = "Please enter a quantity")
                return@launch
            }

            val quantity = state.value.quantity.toDoubleOrNull() ?: 0.0
            val price = state.value.latestPrice

            val transaction = Transaction(
                uid = state.value.currentUserId,
                league_id = state.value.currentLeagueId,
                stock_id = state.value.currentStockId,
                action = if (isBuy) "BUY" else "SELL",
                quantity = quantity,
                price = price,
                transaction_fee = quantity * price * TRANSACTION_FEE
            )

            println("Creating transaction: $transaction")
            TransactionRouter.createTransaction(transaction).fold(
                onSuccess = {
                    // Transaction succeeded, update UI accordingly.
                    _state.value = _state.value.copy(balance = SessionRouter.getSessionBalance(state.value.currentLeagueId, state.value.currentUserId), stockBalance = StockRouter.getStockQuantity(state.value.currentLeagueId, state.value.currentUserId, state.value.currentStockId), quantity = "", message = "Transaction successful")
                },
                onFailure = { error ->
                    // Transaction failed, show error message to the user.
                    _state.value = _state.value.copy(message = "Transaction failed: ${error.message}")
                }
            )
        }
    }

    fun getLeagues() {
        viewModelScope.launch {
            _state.value.sessions = SessionRouter.getUserLeagues(state.value.currentUserId)
            Log.d("StockViewerViewModel", "Leagues: ${_state.value.sessions}")
        }
    }

    private fun startFetchStockData() {
        viewModelScope.launch {
            while (true) {
                try {
                    val stockDetails = StockRouter.getStockDetails(stockTicker)

                    _state.value = _state.value.copy(
                        stockTicker = stockDetails.ticker,
                        currentStockId = stockDetails.id,
                        stockData = stockDetails.priceHistory,
                        stockOpen = stockDetails.open,
                        stockClose = stockDetails.close,
                        stockHigh = stockDetails.high,
                        stockLow = stockDetails.low,
                        latestPrice = stockDetails.latestPrice,
                    )
                } catch (e: Exception) {
                    Log.e("StockViewerViewModel", "Failed to fetch stock data", e)
                    _state.value = _state.value.copy(
                        message = "Failed to fetch stock data: ${e.message}"
                    )
                }
                delay(DATA_FETCHING_DELAY_MS)
            }
        }
    }
} 