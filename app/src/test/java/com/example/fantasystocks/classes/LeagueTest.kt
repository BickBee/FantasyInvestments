package com.example.fantasystocks.classes

import org.junit.Test
import org.junit.Assert.*
import java.util.Calendar

class LeagueTest {
    @Test
    fun testAddPlayers() {
        val player = Player("Alice", 111, 1000.0, 1000.0)
        val league = League(
            Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 12) },
            "Daily Competition"
        )
        league.addPlayer(player)
        assertEquals(league.getPlayers(), listOf(player))
    }
    @Test
    fun testRemovePlayers() {
        val player1 = Player("Alice", 111, 1000.0, 1000.0)
        val player2 = Player("Bob", 112, 1000.0, 1000.0)
        val league = League(
            Calendar.getInstance().apply { set(2025, Calendar.JANUARY, 21) },
            "Weekly Competition"
        )
        league.addPlayer(player1)
        league.addPlayer(player2)
        league.removePlayer(player1)
        assertEquals(league.getPlayers(), listOf(player2))
    }
    @Test
    fun testModifyBalance() {
        val player1 = Player("Alice", 111, 1000.0, 1000.0)
        val player2 = Player("Bob", 112, 1000.0, 1000.0)
        val league = League(
            Calendar.getInstance().apply { set(2025, Calendar.JANUARY, 21) },
            "Weekly Competition"
        )
        league.addPlayer(player1)
        // can't have negative cash
        assertFalse(league.modifyBalance(player1, -100.0))
        assertEquals(1000.0, player1.cash, 0.1)
        // works as intended
        assertTrue(league.modifyBalance(player1, 500.0))
        assertEquals(500.0, player1.cash, 0.1)
        // should only work for players in the league
        assertFalse(league.modifyBalance(player2, 500.0))
        assertEquals(1000.0, player2.cash, 0.1)
    }
    @Test
    fun testChangeEndDate() {
        val start = Calendar.getInstance().apply { set(2025, Calendar.JANUARY, 21) }
        val league = League(start, "Weekly Competition")
        // can't end before today
        val before = Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 5) }
        assertFalse(league.changeEndDate(before))
        assertNull(league.getEndDate())
        // can't end before it starts
        val beforeStart = Calendar.getInstance().apply { set(2025, Calendar.JANUARY, 20) }
        assertFalse(league.changeEndDate(beforeStart))
        assertNull(league.getEndDate())
        // works as intended
        val after = Calendar.getInstance().apply { set(2025, Calendar.MARCH, 21) }
        assertTrue(league.changeEndDate(after))
        assertEquals(after, league.getEndDate())
    }
}