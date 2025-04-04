package com.example.fantasystocks.classes

class Leaderboard(private val players: List<Player>) {
    enum class SortBy {
        NAME,
        TOTAL_RETURNS,
        VALUE
    }

    fun sort(sortBy: SortBy = SortBy.VALUE) : List<Player> {
        val sortedPlayers = when (sortBy) {
            SortBy.NAME -> players.sortedBy { it.name }
            SortBy.TOTAL_RETURNS -> players.sortedByDescending { it.totalReturn() }
            SortBy.VALUE -> players.sortedByDescending { it.getTotalValue() }
        }
        return sortedPlayers
    }
}


