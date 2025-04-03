package com.example.fantasystocks.classes

import com.example.fantasystocks.database.PortfolioRouter
import com.example.fantasystocks.ui.viewmodels.doubleStringToMoneyString
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class PlayerPortfolioView(
    @SerialName("uid")
    val id: String,
    val username: String,
    val cash: Double,
    @SerialName("initial_value")
    val initValue: Double,
    @SerialName("stock_id")
    val stockId: Int?,
    val quantity: Double?,
    @SerialName("stock_name")
    val stockName: String?,
    @SerialName("stock_ticker")
    val stockTicker: String?
)

@Serializable
data class Player (
    val name: String,
    val id: String,
    var initValue: Double,
    var cash: Double,
    val portfolio: MutableMap<Stock, Int> = mutableMapOf()
) {
    fun getTotalValue(leagueId: Int): Double {
        // TODO: get latest historical balance
        var sum = 0.0
        runBlocking {
            sum = PortfolioRouter.getLatestPortfolioValue(id, leagueId) ?: initValue
        }
        return sum
    }

    fun totalReturn(leagueId: Int): Double = (getTotalValue(leagueId) - initValue) / initValue

    private fun positionSize(position: Pair<Stock, Int>) = position.first.price * position.second

    enum class SortBy {
        NAME,
        TICKER,
        VALUE
    }

    fun getPortfolio(sortBy: SortBy = SortBy.NAME ): List<Pair<Stock, Int>> {
        return portfolio.map { (stock, num) -> Pair(stock, num) }
            .sortedWith( when (sortBy) {
                SortBy.NAME -> compareBy { it.first.name }
                SortBy.TICKER -> compareBy { it.first.ticker }
                SortBy.VALUE -> compareByDescending { positionSize(it)}
            })
    }

    fun assetAllocation(sortBy: SortBy = SortBy.VALUE): List<Triple<Stock, Int, Double>> {
        return getPortfolio(sortBy).map {
            Triple(it.first, it.second,positionSize(it) / initValue) // FIXME: replace init value?!
        }
    }
}
