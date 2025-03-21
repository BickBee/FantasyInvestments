package com.example.fantasystocks.classes

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class League(
    @SerialName("league_id")
    val id: Int? = null,
    var name: String,
    @SerialName("start_date")
    var startDate: LocalDate? = null,
    @SerialName("end_date")
    var endDate: LocalDate? = null
) {
    private var players: MutableList<Player> = mutableListOf()

    fun getPlayers(): List<Player> = players
    fun addPlayer(player: Player) = players.add(player)
    fun removePlayer(player: Player) = players.remove(player)
    fun setPlayers(playerList: MutableList<Player>) { players = playerList}
    fun removePlayer(playerId: String) {
        val playerToRemove = players.find { it.id == playerId }
        playerToRemove?.let { players.remove(it) }
    }
    fun removeAllPlayers() { players = mutableListOf() }

    fun getCurrentPlayer(uid: String): Player? {
        return players.find { uid == it.id }
    }

    fun modifyBalance(player: Player, newCash: Double): Boolean {
        if (players.contains(player) && newCash >= 0) {
            player.cash = newCash;
            return true
        }
        return false
    }

    fun changeEndDate(date: LocalDate?): Boolean {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        if (date != null && date >= today && date > startDate!!) {
            endDate = date
            return true
        }
        return false
    }
}