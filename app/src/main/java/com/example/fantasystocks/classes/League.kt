package com.example.fantasystocks.classes

import java.util.Calendar

class League(val startDate: Calendar, var name: String) {
    private val players: MutableList<Player> = mutableListOf()
    private var endDate: Calendar? = null

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

    fun getEndDate() = endDate
    fun changeEndDate(date: Calendar): Boolean {
        if (date.after(Calendar.getInstance()) && date.after(startDate)) {
            endDate = date
            return true
        }
        return false
    }
}