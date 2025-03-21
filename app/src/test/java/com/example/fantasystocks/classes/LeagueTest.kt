package com.example.fantasystocks.classes

import kotlinx.datetime.LocalDate
import org.junit.Test
import org.junit.Assert.*

class LeagueTest {
    @Test
    fun testAddPlayer() {
        val league = League(name = "Test")
        val player = Player("Alice", "103", 1000.0, 115.0, mutableMapOf())
        league.addPlayer(player)
        assertEquals(listOf(player), league.getPlayers())
    }

    @Test
    fun testRemovePlayer() {
        val league = League(name = "Test")
        val player = Player("Alice", "103", 1000.0, 115.0, mutableMapOf())
        league.addPlayer(player)
        league.removePlayer(player)
        assertEquals(listOf<Player>(), league.getPlayers())
    }

    @Test
    fun testModifyBalance() {
        val league = League(name = "Test")
        val player = Player("Alice", "103", 1000.0, 115.0, mutableMapOf())
        val player2 = Player("Alice", "104", 1000.0, 115.0, mutableMapOf())
        league.addPlayer(player)
        assertEquals(true, league.modifyBalance(player, 100.0))
        assertEquals(100.0, player.cash, 0.01)
        assertEquals(false, league.modifyBalance(player, -100.0))
        assertEquals(100.0, player.cash, 0.01)
        assertEquals(false, league.modifyBalance(player2, 100.0))
    }

    @Test
    fun testChangeEndDate() {
        val league = League(name = "Test", startDate = LocalDate(2025,1,1))
        assertEquals(true, league.changeEndDate(LocalDate(2035, 1, 1)))
        assertEquals(LocalDate(2035, 1, 1), league.endDate)
        val league2 = League(name = "Test", startDate = LocalDate(2024, 1, 1))
        assertEquals(false, league2.changeEndDate(LocalDate(2024, 4,1)))
        assertEquals(null, league2.endDate)
        val league3 = League(name = "Test", startDate = LocalDate(2035, 1, 1))
        assertEquals(false, league3.changeEndDate(LocalDate(2034, 1, 1)))
        assertEquals(null, league3.endDate)
    }
}