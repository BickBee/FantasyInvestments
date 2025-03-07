package com.example.fantasystocks.ui.viewmodels

import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.fantasystocks.classes.League
import com.example.fantasystocks.classes.User
import com.example.fantasystocks.models.HomeModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.time.ZoneOffset
import java.util.Locale
import java.util.Date
import java.util.Calendar

fun Long.morning(): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = this@morning
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

fun localToUTC(): Long {
    val utc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        java.time.LocalDate.now().atTime(0,0,0,0).toEpochSecond(ZoneOffset.UTC) * 1000
        else System.currentTimeMillis()
    return System.currentTimeMillis() - utc
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun longToLocalDate(millis: Long?): LocalDate? {
    return millis?.let {
        Instant.fromEpochMilliseconds(it)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    }
}

fun doubleStringToMoneyString(num: String): String {
    val length = num.length
    val result = StringBuilder()
    var count = 0
    for (i in length - 1 downTo 0) {
        result.append((num[i]))
        count++
        if (count % 3 == 0 && i != 0) {
            result.append(',')
        }
    }
    return result.reverse().toString()
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HomeViewModel: ViewModel() {
    private val homeModel = HomeModel()

    // --------------- Init loading ---------------
    var initLoading by mutableStateOf(false)
        private set
    // --------------- END Init loading ---------------

    // --------------- Dialog ---------------
    var isDialogShown by mutableStateOf(false)
        private set
    fun openLeagueDialog() { isDialogShown = true }
    fun closeLeagueDialog() {
        updateLeagueName("")
        //updatePlayerName("")
        players = emptyList()
        useCashForAll = false
        cashForAll = 10000
        isDialogShown = false
    }
    // --------------- END Dialog ---------------

    // league name max len = 30
    var leagueName by mutableStateOf("")
        private set
    fun updateLeagueName(input: String) {
        if (input.length <= 30) {
            leagueName = input
        }
    }


    // date
    var startDate by mutableStateOf(System.currentTimeMillis())
        private set
    var endDate by mutableStateOf<Long?>(null)
        private set

    fun updateStartDate(newDate: Long) {
        if (newDate >= System.currentTimeMillis().morning()) {
            startDate = newDate
        }
    }
    fun updateEndDate(newDate: Long) {
        if (newDate.morning() > System.currentTimeMillis().morning()) {
            endDate = newDate
        }
    }


    // cash
    var useCashForAll by mutableStateOf(false)
        private set
    fun setCashForAll(checked: Boolean) { useCashForAll = checked }
    var cashForAll by mutableStateOf(10000)
        private set

    fun updateCashForAll(cash: String) {
        val withoutCommas = cash.replace(",", "")
        val value = withoutCommas.toInt()
        cashForAll = when {
            (value < 0) -> 0
            (value > 99999999) -> 99999999
            else -> value
        }
    }

    fun cashToString(): String {
        return doubleStringToMoneyString(cashForAll.toString())
    }



    // players
    var players by mutableStateOf(listOf<User>())
        private set
    var addPlayerField by mutableStateOf(false)
        private set

    fun openPlayerField() { addPlayerField = true }
    fun closePlayerField() { addPlayerField = false }
    var playerName by mutableStateOf("")
        private set

    // --------------- Search Bar ---------------
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    fun onPlayerSearch(input: String) {
        _searchText.value = input
        viewModelScope.launch {
            val res = homeModel.getUsersLike(input, 5)
        }
        /*

        viewModelScope.launch {
            searchText
                .debounce(500L)
                .distinctUntilChanged()
                .filter { it.isNotEmpty() }
                .onEach { _isSearching.value = true }
                .flatMapLatest { text ->
                    getUsersLike(text, 5)
                }
                .onEach { _isSearching.value = false }
                .catch { e ->   // finish loading if fails
                    _isSearching.value = false
                    println("ERROR IN USERS FLOW: ${e.message}")
                }
                .collect { results ->
                    _users.value = results
                }
        }

         */
    }

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _users = MutableStateFlow(listOf<User>())
    val users = _users.asStateFlow()

    private fun getUsersLike(query: String, lim: Long): Flow<List<User>> = flow {
        try {
            val users = homeModel.getUsersLike(query, lim)
            emit(users)
        } catch (e: Exception) {
            println("ERROR GETTING USERS FLOW: ${e.message}")
            emit(emptyList())
        }
    }


     /*
        .debounce(500L)
        .onEach { _isSearching.update { true } }
        .flatMapLatest { text ->
            flow {
                val res = if (text.isBlank()) {
                    _users.value
                } else {
                    createLeagueModel.getUsersLike(text, 5)
                }
                emit(listOf<User>(User(1, "alice", "alice@ex.com", "alice", "pass")))
            }
        }
        .onEach { _isSearching.update { false } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _users.value
        )
      */
    // --------------- END Search Bar ---------------

    /*
    fun updatePlayerName(input: String) {
        playerName = input
    }
     */

    fun deletePlayerName() {
        playerName = ""
        closePlayerField()
    }
    /*
    fun addPlayer() {
        players = players + Player(playerName, 101, 1000.0, 1000.0)
        playerName = ""
        closePlayerField()
    }
     */
    fun removePlayer(player: User) { players = players.filter { it != player} }

    // --------------- Create League ---------------
    private val _leagueCreationResult = MutableStateFlow<League?>(null)
    val leagueCreationResult = _leagueCreationResult.asStateFlow()

    fun createLeague() {
        val newLeague = League(
            name = leagueName,
            startDate = longToLocalDate(startDate),
            endDate = longToLocalDate(endDate)
        )
        viewModelScope.launch {
            val result =  homeModel.insertLeagueAndReturn(newLeague)
            _leagueCreationResult.value = result
            closeLeagueDialog()
        }
    }

    fun resetLeagueCreationResult() {
        _leagueCreationResult.value = null
    }

    // --------------- END Create League ---------------

    // --------------- Retrieve leagues ---------------
    private val _leagues = MutableStateFlow<List<League>>(emptyList())
    val leagues = _leagues.asStateFlow()
    fun getLeagues() {
        viewModelScope.launch {
            initLoading = true
            val result = homeModel.getLeagues()
            _leagues.value = result
            initLoading = false
        }
    }
    // --------------- END Retrieve leagues ---------------

}


