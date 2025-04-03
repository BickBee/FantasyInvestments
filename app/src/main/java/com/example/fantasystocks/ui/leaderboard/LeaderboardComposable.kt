package com.example.fantasystocks.ui.leaderboard

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fantasystocks.classes.Leaderboard
import com.example.fantasystocks.classes.Player
import com.example.fantasystocks.ui.viewmodels.LeagueViewModel
import com.example.fantasystocks.ui.viewmodels.doubleMoneyToString
import com.example.fantasystocks.ui.viewmodels.doubleStringToMoneyString
import java.util.Locale

@Composable
fun LeaderboardComposable(
    viewModel: LeagueViewModel,
    leaderboard: Leaderboard?
) {
    val leagueState = viewModel.league.collectAsState()
    val leagueId = leagueState.value?.id!!
    if (leaderboard == null) {
        CircularProgressIndicator()
    } else {
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
                TopLeaderboard(leagueId, top3)
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
                BottomLeaderboard(leagueId, rest)
            }
        }
    }
}

@Composable
fun TopLeaderboard(
    leagueId: Int,
    players: List<Player>   // [1st, 2nd, 3rd]
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
                TopItem(players[1], "🥈", leagueId)
            }
        }
        if (players.size >= 1) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                TopItem(players[0], "🥇", leagueId)
            }
        }
        if (players.size >= 3) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.BottomCenter
            ) {
                TopItem(players[2], "🥉", leagueId)
            }
        }
    }
}

@Composable
fun TopItem(
    player: Player,
    medal: String,
    leagueId: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.AccountCircle,
            contentDescription = "Profile picture",
            Modifier.size(100.dp)
        )
        Text(
            text = medal,
            fontSize = 32.sp
        )
        Text(
            text = player.name,
            fontWeight = FontWeight.Bold
        )
        Text(text = doubleMoneyToString(player.getTotalValue(leagueId)))
    }
}


@Composable
fun BottomLeaderboard(
    leagueId: Int,
    players: List<Player> // [p4, p5, ...]
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
                LeaderboardItem(idx + 4, player, leagueId)
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
fun LeaderboardItem(rank: Int, player: Player, leagueId: Int) {
    val cardColor = when (player.name) {
        "You" -> MaterialTheme.colorScheme.primary
        else -> Color.White
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
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
                Icon(
                    Icons.Outlined.AccountCircle,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(28.dp)
                )
            }
            Text(
                text = player.name,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = doubleMoneyToString(player.getTotalValue(leagueId)),
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
