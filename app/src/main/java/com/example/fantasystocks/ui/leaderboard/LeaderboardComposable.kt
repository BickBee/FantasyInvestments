package com.example.fantasystocks.ui.leaderboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fantasystocks.classes.Leaderboard
import com.example.fantasystocks.classes.League
import com.example.fantasystocks.classes.Player
import com.example.fantasystocks.database.StockRouter
import com.example.fantasystocks.ui.components.UserAvatar
import com.example.fantasystocks.ui.screens.OtherPlayersPortfolio
import com.example.fantasystocks.ui.theme.ThemeManager
import com.example.fantasystocks.ui.viewmodels.LeagueViewModel
import com.example.fantasystocks.ui.viewmodels.doubleMoneyToString
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun LeaderboardComposable(
    viewModel: LeagueViewModel,
    goToOtherPlayersPortfolio: (String, Int) -> Unit
) {
    val league by viewModel.league.collectAsState()
    val players by viewModel.players.collectAsState()

    var stockPrices by remember { mutableStateOf<Map<Int, List<Double>>>(emptyMap()) }
    LaunchedEffect(Unit) {
        league?.let { league ->
            val stockIds = league.allStockIds()
            while (true) {
                stockPrices = StockRouter.getStockPriceMapWithClose(stockIds)
                viewModel.updatePortfolios(stockPrices)
                delay(3000)
            }
        }
    }

    if (league == null || players.isEmpty()) {
        CircularProgressIndicator()
    } else {
        val leaderboard = Leaderboard(players)
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val sortedPlayers = leaderboard.sort(Leaderboard.SortBy.VALUE)
            val top3 = if (sortedPlayers.size >= 3) sortedPlayers.subList(0, 3) else sortedPlayers
            val rest = if (sortedPlayers.size > 3) sortedPlayers.subList(3, sortedPlayers.size) else emptyList()
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxSize()
            ) {
                TopLeaderboard(top3, league!!.id!!, goToOtherPlayersPortfolio)
            }
            Card(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxSize(),
                shape = RoundedCornerShape(
                    topStart = 32.dp,
                    topEnd = 32.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            ) {
                BottomLeaderboard(rest, league!!.id!!, goToOtherPlayersPortfolio)
            }
        }
    }
}

@Composable
fun TopLeaderboard(
    players: List<Player>,  // [1st, 2nd, 3rd]
    leagueId: Int,
    goToOtherPlayersPortfolio: (String, Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (players.size >= 2) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.BottomCenter
            ) {
                TopItem(players[1], "🥈", leagueId, goToOtherPlayersPortfolio)
            }
        }
        if (players.size >= 1) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                TopItem(players[0], "🥇", leagueId, goToOtherPlayersPortfolio)
            }
        }
        if (players.size >= 3) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.BottomCenter
            ) {
                TopItem(players[2], "🥉", leagueId, goToOtherPlayersPortfolio)
            }
        }
    }
}

@Composable
fun TopItem(
    player: Player,
    medal: String,
    leagueId: Int,
    goToOtherPlayersPortfolio: (String, Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable {
            val json = Json { allowStructuredMapKeys = true }
            goToOtherPlayersPortfolio(json.encodeToString(player), leagueId)
        }
    ) {
        UserAvatar(
            avatarId = player.avatarId,
            username = player.name,
            size = 90
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = medal,
            fontSize = 32.sp
        )
        Text(
            text = player.name,
            fontWeight = FontWeight.Bold
        )
        Text(text = doubleMoneyToString(player.getTotalValue()))
    }
}


@Composable
fun BottomLeaderboard(
    players: List<Player>, // [p4, p5, ...]
    leagueId: Int,
    goToOtherPlayersPortfolio: (String, Int) -> Unit
) {
    if (players.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    top = 20.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
        ) {
            itemsIndexed(players) { idx, player ->
                LeaderboardItem(idx + 4, player, leagueId, goToOtherPlayersPortfolio)
            }
        }
    } else {
        Text(
            text = "Add More Players For A Better Experience",
            modifier = Modifier.padding(32.dp),
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun LeaderboardItem (
    rank: Int,
    player: Player,
    leagueId: Int,
    goToOtherPlayersPortfolio: (String, Int) -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme.collectAsState()

    val cardColor = when (isDarkTheme) {
        true -> Color.Black
        false -> Color.White
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable {
                val json = Json { allowStructuredMapKeys = true }
                goToOtherPlayersPortfolio(json.encodeToString(player), leagueId)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${rank}.",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(0.2f)
            )
            Box(modifier = Modifier.weight(0.3f)) {
                UserAvatar(
                    avatarId = player.avatarId,
                    username = player.name,
                    size = 28,
                )
            }
            Text(
                text = player.name,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
//                text = doubleMoneyToString(player.getTotalValue(leagueId)),
                text = doubleMoneyToString(player.getTotalValue()),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


// -----------------
// might use future (was from old leaderboard table)
@Composable
fun LeaderboardHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Rank",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.5f)
        )
        Text(
            text = "Name",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Value",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
    HorizontalDivider(thickness = 2.dp)
}
