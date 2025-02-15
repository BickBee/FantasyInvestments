package com.example.fantasystocks.classes

class Leaderboard(private val league: League) {
    enum class SortBy {
        NAME,
        TOTAL_RETURNS
    }

    fun sort(sortBy: SortBy = SortBy.TOTAL_RETURNS) : List<Player> {
        val sortedPlayers = when (sortBy) {
            SortBy.NAME -> league.getPlayers().sortedBy { it.name }
            SortBy.TOTAL_RETURNS -> league.getPlayers().sortedByDescending { it.totalReturn() }
        }
        return sortedPlayers
    }
}


