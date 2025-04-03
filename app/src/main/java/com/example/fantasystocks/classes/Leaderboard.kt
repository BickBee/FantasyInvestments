package com.example.fantasystocks.classes

class Leaderboard(private val league: League) {
    enum class SortBy {
        NAME,
        TOTAL_RETURNS,
        VALUE
    }

    fun sort(sortBy: SortBy = SortBy.VALUE) : List<Player> {
        val sortedPlayers = when (sortBy) {
            SortBy.NAME -> league.getPlayers().sortedBy { it.name }
            SortBy.TOTAL_RETURNS -> league.getPlayers().sortedByDescending { it.totalReturn(league.id!!) }
            SortBy.VALUE -> league.getPlayers().sortedByDescending { it.getTotalValue(league.id!!) }
        }
        return sortedPlayers
    }
}


