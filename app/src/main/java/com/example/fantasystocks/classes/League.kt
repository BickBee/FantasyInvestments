package com.example.fantasystocks.classes

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class League(
    val id: Int? = null,
    var name: String,
    @SerialName("start_date")
    var startDate: LocalDate? = null,
    @SerialName("end_date")
    var endDate: LocalDate? = null)
{
    private val players: MutableList<Player> = mutableListOf()

    fun getPlayers(): List<Player> = players
    fun addPlayer(player: Player) = players.add(player)
    fun removePlayer(player: Player) = players.remove(player)

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