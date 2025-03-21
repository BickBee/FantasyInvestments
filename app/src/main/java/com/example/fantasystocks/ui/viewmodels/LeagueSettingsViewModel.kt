package com.example.fantasystocks.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fantasystocks.classes.League
import com.example.fantasystocks.classes.Player
import com.example.fantasystocks.classes.User
import com.example.fantasystocks.classes.UserLeague
import com.example.fantasystocks.database.SupabaseClient
import com.example.fantasystocks.models.DateType
import com.example.fantasystocks.models.LeagueModel
import com.example.fantasystocks.models.UserInformation
import com.example.fantasystocks.models.UserLeagueModel
import com.example.fantasystocks.models.UserModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class LeagueSettingsViewModelFactory(
    private val league: League
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeagueSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LeagueSettingsViewModel(league) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(FlowPreview::class)
class LeagueSettingsViewModel(val league: League): ViewModel() {
    private val leagueModel = LeagueModel()
    private val userModel = UserModel()
    private val userLeagueModel = UserLeagueModel()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    fun resetError() { _error.value = null }

    private val _league = MutableStateFlow(league)
    val leagueState: StateFlow<League> = _league

    // -------- League Name --------
    var isLeagueNameShown by mutableStateOf(false)
        private set
    fun editLeagueName() { isLeagueNameShown = true }
    fun closeLeagueName() { isLeagueNameShown = false }

    var newLeagueName by mutableStateOf(league.name)
        private set
    fun editNewLeagueName(input: String) {
        if (input.length <= 20) {
            newLeagueName = input
        }
    }
    fun updateLeagueName(newName: String) {
        viewModelScope.launch {
            try {
                if (newLeagueName.isBlank()) {
                    throw IllegalArgumentException("League name cannot be blank")
                }
                if (newLeagueName == league.name) {
                    throw IllegalArgumentException("New league name must be different")
                }
                val leagueId = requireNotNull(league.id) { "NULL LEAGUE ID, CANNOT UPDATE LEAGUE NAME" }
                leagueModel.updateLeagueName(newName, league.id)
                // update the league
                val updatedLeague = league.copy(name = newLeagueName)
                _league.value = updatedLeague
            } catch (e: Exception) {
                println(e)
            }
        }
    }
    // -------- END League Name --------

    // -------- Players & Search --------
    var isPlayersShown by mutableStateOf(false)
        private set
    fun editPlayers() { isPlayersShown = true }
    fun closePlayers() { isPlayersShown = false }
    fun removePlayer(playerId: String) {
        viewModelScope.launch {
            leagueModel.removePlayer(playerId, league.id!!)
        }
        _players.value = _players.value.filter { it.id != playerId }
    }

    var isPlayerSearch by mutableStateOf(false)
        private set
    fun openPlayerSearch() { isPlayerSearch = true }
    fun closePlayerSearch() { isPlayerSearch = false }

    private val _players = MutableStateFlow<List<Player>>(_league.value.getPlayers())
    val players: StateFlow<List<Player>> = _players

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

    fun addPlayerToLeague() {
        if (cashIsOpenFor != null) {
            val newPlayer =
                Player(
                    name = cashIsOpenFor!!.username,
                    id = cashIsOpenFor!!.uid,
                    initValue = newPlayerCash.toDouble(),
                    cash = newPlayerCash.toDouble()
                )
            _players.value += newPlayer
            viewModelScope.launch {
                userLeagueModel.insertUserLeague(
                    UserLeague(
                        uid = newPlayer.id,
                        leagueId = league.id,
                        cash = newPlayer.cash,
                        initValue = newPlayer.initValue
                    )
                )
            }
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
        searchQuery.value = ""
    }
    // -------- END Players & Search --------

    // -------- Cash --------
    var isNewPlayerCashOpen by mutableStateOf(false)
    fun openNewPlayerCash() { isNewPlayerCashOpen = true}
    fun closeNewPlayerCash() { isNewPlayerCashOpen = false}

    var cashIsOpenFor by mutableStateOf<UserInformation?>(null)
        private set
    fun updateCashIsOpenFor(user: UserInformation) { cashIsOpenFor = user }

    var newPlayerCash by mutableStateOf(10000)
        private set
    fun updateNewPlayerCash(cash: String) { newPlayerCash = stringCashToInt(cash) }
    // -------- END Cash --------

    // -------- Start Date --------
    var isStartShown by mutableStateOf(false)
        private set

    fun openStartDate() {
        _error.value = null
        // open if league hasn't started yet
        if (localDateToLong(league.startDate)!! < System.currentTimeMillis()) {
            _error.value = "League has already started!"
        } else {
            isStartShown = true
        }
    }
    fun closeStartDate() { isStartShown = false }

    fun updateStartDate(newDate: Long) {
        _error.value = null
        val newLocalDate = longToLocalDate(newDate)
        // cant start after end
        if (_league.value.endDate!! < newLocalDate!!) {
            _error.value = "Can't start after end date!"
            return
        }
        try {
            viewModelScope.launch {
                if (newDate >= System.currentTimeMillis().morning()) {
                    leagueModel.updateDate(league.id!!, newLocalDate, DateType.START)
                    val updatedLeague = _league.value.copy(startDate = newLocalDate)
                    _league.value = updatedLeague
                }
            }
        } catch (e: Exception) {
                println("ERROR UPDATING START DATE: $e")
        }
    }
    // -------- END Start Date --------

    // -------- End Date --------
    var isEndShown by mutableStateOf(false)
        private set
    fun openEndDate() { isEndShown = true }
    fun closeEndDate() { isEndShown = false }

    fun updateEndDate(newDate: Long) {
        val newLocalDate = longToLocalDate(newDate)
        try {
            viewModelScope.launch {
                if (newLocalDate!! > _league.value.startDate!!) {
                    leagueModel.updateDate(league.id!!, newLocalDate, DateType.END)
                    val updatedLeague = _league.value.copy(endDate = newLocalDate)
                    _league.value = updatedLeague
                }
            }
        } catch (e: Exception) {
            println("ERROR UPDATING END DATE: $e")
        }
    }
    // -------- END End Date --------

    // -------- Leave Group --------
    var isLeaveGroup by mutableStateOf(false)
        private set
    fun openLeaveGroup() { isLeaveGroup = true }
    fun closeLeaveGroup() { isLeaveGroup = false }
    fun leaveGroup() {
        try {
            val userId = requireNotNull(SupabaseClient.getCurrentUID()) {
                "CANNOT LEAVE GROUP, NULL UID"
            }
            removePlayer(userId)
        } catch (e: Exception) {
            println(e)
        }
    }
    // -------- END Leave Group --------
}
