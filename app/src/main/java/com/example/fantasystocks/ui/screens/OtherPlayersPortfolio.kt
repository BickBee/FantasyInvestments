package com.example.fantasystocks.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fantasystocks.classes.Player
import com.example.fantasystocks.ui.components.StockGraph
import com.example.fantasystocks.ui.portfolio.PortfolioAndActivity
import com.example.fantasystocks.ui.theme.InvalidRed
import com.example.fantasystocks.ui.theme.PositiveGreen
import com.example.fantasystocks.ui.viewmodels.LeagueViewModel
import com.example.fantasystocks.ui.viewmodels.doubleMoneyToString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.abs

@Serializable
data class OtherPlayersPortfolio(val playerJson: String, val leagueId: Int)

fun NavGraphBuilder.otherPlayersPortfolio() {
    composable<OtherPlayersPortfolio> { backStackEntry ->
        val playerJson = backStackEntry.arguments?.getString("playerJson")
        val leagueId = backStackEntry.arguments?.getInt("leagueId") ?: -1
        val json = Json { allowStructuredMapKeys = true }
        val player = json.decodeFromString<Player>(playerJson ?: "")
        OtherPlayersPortfolio(player, leagueId)
    }
}

@Composable
fun OtherPlayersPortfolio(
    player: Player,
    leagueId: Int
) {
    val viewModel: LeagueViewModel = viewModel<LeagueViewModel>()
    val historicalValues by viewModel.historicalValues.collectAsState()
    val historicalLoading by viewModel.historicalLoading.collectAsState()
    val currentPLayer by viewModel.currentPlayer.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setCurrentPlayer(player)
        viewModel.getHistoricalValues(player.id, leagueId, player.initValue)
    }

    if (currentPLayer != null) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val priceChange = player.getTotalValue() - player.initValue
            val percentChange = 100 * priceChange / player.initValue
            Text(
                text = "${player.name}'s Portfolio",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = doubleMoneyToString(currentPLayer!!.getTotalValue()),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 22.dp)
            )
            Text(
                text = buildString {
                    append(if (percentChange >= 0) "+" else "-")
                    append("%.2f".format(abs(percentChange)))
                    append("% ")
                    append(
                        if (priceChange >= 0) {
                            doubleMoneyToString(priceChange)
                        } else "(${doubleMoneyToString(abs(priceChange))})"
                    )
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (percentChange >= 0) PositiveGreen else InvalidRed,
                modifier = Modifier
                    .padding(vertical = 4.dp)
            )
            Box(
                modifier = Modifier
                    .padding(
                        top = 2.dp,
                        bottom = 2.dp,
                        start = 8.dp,
                        end = 20.dp
                    )
            ) {
                if (historicalLoading || historicalValues == null) {
                    CircularProgressIndicator()
                } else {
                    if (currentPLayer!!.portfolio.isEmpty()) {
                        val cash = currentPLayer!!.cash
                        val initValue = currentPLayer!!.initValue
                        StockGraph(listOf(initValue, initValue, cash, cash - 0.00001))
                    } else {
                        StockGraph(historicalValues!!)
                    }
                }
            }
            PortfolioAndActivity(
                player = player,
                leagueId = leagueId,
                uid = player.id,
                viewModel = viewModel,
                canClick = false,
                goToStockViewer = { println("clicked") }
            )
        }
    }
}