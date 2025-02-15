package com.example.fantasystocks.classes

import org.junit.Test
import org.junit.Assert.*
import java.util.Calendar

class LeaderboardTest {
    @Test
    fun testSort() {
        val stock1 = Stock("Apple", "APPL", 235.26)
        val stock2 = Stock("Boeing", "BA", 30.82)
        val stock3 = Stock("General Motors", "GM", 400.41)
        val player1 = Player("Bob", 103, 1000.0, 100.0,
            mutableMapOf(Pair(stock1, 1)))
        val player2 = Player("Alice", 104, 1000.0, 100.0,
            mutableMapOf(Pair(stock2, 1)))
        val player3 = Player("Charlie", 105, 1000.0, 100.0,
            mutableMapOf(Pair(stock3, 1)))
        val start = Calendar.getInstance().apply { set(2025, Calendar.JANUARY, 21) }
        val league = League(start, "Competition")
        league.addPlayer(player1)
        league.addPlayer(player2)
        league.addPlayer(player3)
        val leaderboard = Leaderboard(league)
        // test enumerations
        assertEquals(listOf(player2, player1, player3), leaderboard.sort(Leaderboard.SortBy.NAME))
        assertEquals(listOf(player3, player1, player2), leaderboard.sort())
    }
}