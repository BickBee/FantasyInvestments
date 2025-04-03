package com.example.fantasystocks.ui.viewmodels

import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fantasystocks.LEAGUE_DATA_FETCHING_DELAY_MS
import com.example.fantasystocks.classes.League
import com.example.fantasystocks.classes.Player
import com.example.fantasystocks.classes.UserLeague
import com.example.fantasystocks.database.SupabaseClient
import com.example.fantasystocks.models.LeagueModel
import com.example.fantasystocks.models.UserInformation
import com.example.fantasystocks.models.UserLeagueModel
import com.example.fantasystocks.models.UserModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

fun localDateToLong(date: LocalDate?): Long? {
    return date?.atStartOfDayIn(TimeZone.currentSystemDefault())?.toJavaInstant()?.toEpochMilli()
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

fun stringCashToInt(cash: String): Int {
    val withoutCommas = cash.replace(",", "")
    val value = withoutCommas.toInt()
    return when {
        (value < 0) -> 0
        (value > 99999999) -> 99999999
        else -> value
    }
}

fun cashToString(cash: Int): String {
    return doubleStringToMoneyString(cash.toString())
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HomeViewModel: ViewModel() {
    private val userLeagueModel = UserLeagueModel()
    private val leagueModel = LeagueModel()
    private val userModel = UserModel()

    // --------------- Init loading ---------------
    var initLoading by mutableStateOf(false)
        private set
    // --------------- END Init loading ---------------

    // --------------- Dialog ---------------
    var isDialogShown by mutableStateOf(false)
        private set
    fun openLeagueDialog() {
        isDialogShown = true
        viewModelScope.launch {
            try {
                val currentUser = userModel.getUserInfo(SupabaseClient.getCurrentUID()!!)
                _players.value += Player(
                    name = currentUser!!.username,
                    id = currentUser.uid,
                    cash = cashForAll.toDouble(),
                    initValue = cashForAll.toDouble()
                )
            } catch (e: Exception) {
                println("ERROR GETTING CURRENT PLAYER: $e")
            }
        }
    }
    fun closeLeagueDialog() {
        updateLeagueName("")
        _players.value = emptyList()
        useCashForAll = true
        cashForAll = 10000
        resetDates()
        closePlayerField()
        isDialogShown = false
        searchQuery.value = ""
        _searchResults.value = emptyList()
    }
    // --------------- END Dialog ---------------

    // --------------- League Name (30 max) ---------------
    var leagueName by mutableStateOf("")
        private set
    fun updateLeagueName(input: String) {
        if (input.length <= 30) {
            leagueName = input
        }
    }
    // --------------- END League Name ---------------


    // --------------- Date ---------------
    var startDate by mutableStateOf(System.currentTimeMillis())
        private set
    var endDate by mutableStateOf<Long?>(null)
        private set

    private fun resetDates() {
        startDate = System.currentTimeMillis()
        endDate = null
    }

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
    // --------------- END Date ---------------


    // --------------- Cash ---------------
    var useCashForAll by mutableStateOf(true)
        private set
    fun setCashForAll(checked: Boolean) { useCashForAll = checked }
    var cashForAll by mutableStateOf(10000)
        private set

    var isCashOpen by mutableStateOf(false)
        private set
    fun openCash() { isCashOpen = true }
    fun closeCash() { isCashOpen = false }

    var cashIsOpenFor by mutableStateOf("")
        private set
    fun updateCashIsOpenFor(username: String) { cashIsOpenFor = username}

    var singleCash by mutableStateOf(10000)
        private set

    fun updateSingleCash(cash: String) { singleCash = stringCashToInt(cash) }
    fun updateSingleCash(cash: Int) { singleCash = cash}

    fun updateCashForAll(cash: String) {
        cashForAll = stringCashToInt(cash)
        _players.value = _players.value.map { player ->
            player.copy(cash = cashForAll.toDouble(), initValue = cashForAll.toDouble())
        }
    }

    fun updateSingleCash() {
        _players.value = _players.value.map { player ->
            if (player.name == cashIsOpenFor) {
                player.copy(cash = singleCash.toDouble(), initValue = singleCash.toDouble())
            } else {
                player
            }
        }
    }
    // --------------- END Cash ---------------



    // --------------- Players ---------------
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players

    fun removePlayer(player: Player) { _players.value = _players.value.filter { it != player} }


    var addPlayerField by mutableStateOf(false)
        private set
    fun openPlayerField() { addPlayerField = true }
    fun closePlayerField() { addPlayerField = false }

    private val _userSearchLoading = MutableStateFlow(false)
    val userSearchLoading: StateFlow<Boolean> = _userSearchLoading.asStateFlow()

    private val _searchResults = MutableStateFlow<List<UserInformation>>(emptyList())
    val searchResults: StateFlow<List<UserInformation>> = _searchResults.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(250)
                .collectLatest { query ->
                    if (query.isNotEmpty()) {
                        performSearch(query)
                    }
                }
        }
    }

    fun searchUsers(query: String) { searchQuery.value = query }

    private suspend fun performSearch(query: String) {
        _userSearchLoading.value = true
        try {
            val users = userModel.getUsersLike(query, 10)
            val filteredUsers = users.filter { user -> !_players.value.any { it.name == user.username } }
            _searchResults.value = filteredUsers.take(5)
        } catch (e: Exception) {
            _searchResults.value = emptyList()
        } finally {
            _userSearchLoading.value = false
        }
    }

    fun addPlayerToLeague(player: UserInformation) {
        val newPlayer =
            Player(
                name = player.username,
                id = player.uid,
                initValue = cashForAll.toDouble(),
                cash = cashForAll.toDouble()
            )
        _players.value += newPlayer
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
        searchQuery.value = ""
    }

    // --------------- END Players ---------------





    // --------------- Create League ---------------
    private val _leagueCreationResult = MutableStateFlow<Int?>(null)
    val leagueCreationResult = _leagueCreationResult.asStateFlow()

    @Serializable
    data class LeagueCreationDTO(
        val name: String,
        val startDate: LocalDate? = null,
        val endDate: LocalDate? = null,
    )

    fun createLeague() {
        viewModelScope.launch {
            // insert league first
            val newLeague = League(
                name = leagueName,
                startDate = longToLocalDate(startDate),
                endDate = longToLocalDate(endDate)
            )
            val result =  leagueModel.insertLeagueAndReturnId(newLeague)
            // insert user_leagues next
            val userLeagues = _players.value.map {
                UserLeague(
                    uid = it.id,
                    leagueId = result,
                    cash = if (useCashForAll) cashForAll.toDouble() else it.cash,
                    initValue = if (useCashForAll) cashForAll.toDouble() else it.initValue
                )
            }
            userLeagueModel.insertUserLeagueList(userLeagues)
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
    fun getUsersLeagues() {
        viewModelScope.launch {
            initLoading = true
            while (true) {
                try {
                    val userId = requireNotNull(SupabaseClient.getCurrentUID()) {
                        "NULL UID WHILE FETCHING USERS LEAGUES"
                    }
                    val userLeagues = userLeagueModel.getUsersLeagues(userId)
                    val leagueIds = userLeagues.mapNotNull { it.leagueId }
                    val result = leagueModel.getLeagues(leagueIds)
                    _leagues.value = result
                } catch (e: Exception) {
                    println(e)
                } finally {
                    initLoading = false
                }
                delay(LEAGUE_DATA_FETCHING_DELAY_MS)
                Log.d("HomeViewModel", "Fetched more leagues")
            }
        }
    }
    // --------------- END Retrieve leagues ---------------

}


