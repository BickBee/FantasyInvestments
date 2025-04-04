package com.example.fantasystocks.classes

import org.junit.Test
import org.junit.Assert.*

class PlayerTest {
    @Test
    fun testGetTotalValue() {
        val stock1 = Stock(1, "Apple", "AAPL", 135.10)
        val stock2 = Stock(2, "Google", "GOOG", 210.87)
        val player = Player(
            "Alice",
            "103",
            1,
            1000.0,
            115.0,
            mutableMapOf(Pair(stock1, 3), Pair(stock2, 2))
        )
        assertEquals(942.04, player.getTotalValue(), 0.01)
    }

    @Test
    fun testTotalReturn() {
        val stock1 = Stock(1, "Microsoft", "MSFT", 58.92)
        val player = Player(
            "Bob",
            "118",
            1,
            1000.0,
            400.57,
            mutableMapOf(Pair(stock1, 5))
        )
        assertEquals(-0.30483, player.totalReturn(), 0.00001)
    }

    @Test
    fun testGetPortfolio() {
        val stock1 = Stock(1, "Tesla", "TSLA", 332.16)
        val stock2 = Stock(2, "NVIDIA", "NVDA", 130.59)
        val stock3 = Stock(3, "Google", "GOOG", 102.42)
        val player = Player(
            "Charlie",
            "129",
            2,
            10000.0,
            2500.0,
            mutableMapOf(Pair(stock1, 2), Pair(stock2, 5), Pair(stock3, 10))
        )
        assertEquals(
            listOf(Pair(stock3, 10), Pair(stock2, 5), Pair(stock1, 2)),
            player.getPortfolio(Player.SortBy.NAME)
        )
        assertEquals(
            listOf(Pair(stock3, 10), Pair(stock2, 5), Pair(stock1, 2)),
            player.getPortfolio(Player.SortBy.TICKER)
        )
        assertEquals(
            listOf(Pair(stock3, 10), Pair(stock1, 2), Pair(stock2, 5)),
            player.getPortfolio(Player.SortBy.VALUE)
        )
    }

    @Test
    fun testAssetAllocation() {
        val stock1 = Stock(1, "Apple", "APPL", 235.26)
        val stock2 = Stock(2, "Boeing", "BA", 30.82)
        val stock3 = Stock(3,"General Motors", "GM", 400.41)
        val stock4 = Stock(4, "NVIDIA", "NVDA", 130.59)
        val player = Player(
            "Dave",
            "1192",
            3,
            5000.0,
            1187.13,
            mutableMapOf(Pair(stock1, 2), Pair(stock2, 15), Pair(stock3, 2), Pair(stock4, 5))
        )
        val allocation = player.assetAllocation()
        assertEquals(0.160164, allocation[0].third, 0.0001)
        assertEquals(0.13059, allocation[1].third, 0.0001)
        assertEquals(0.094104, allocation[2].third, 0.0001)
        assertEquals(0.09246, allocation[3].third, 0.0001)
    }
}