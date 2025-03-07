package com.example.fantasystocks.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fantasystocks.classes.Leaderboard
import com.example.fantasystocks.classes.League
import com.example.fantasystocks.classes.Player
import com.example.fantasystocks.ui.leaderboard.LeaderboardComposable
import com.example.fantasystocks.ui.portfolio.Portfolio
import com.example.fantasystocks.ui.viewmodels.LeagueViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale

@Serializable
data class LeagueScreen(val leagueJson: String)

fun NavGraphBuilder.leagueScreenViewer(goToStockViewer: (String) -> Unit) {
    composable<LeagueScreen> { backStackEntry ->
        val leagueJson = backStackEntry.arguments?.getString("leagueJson")
        val league = Json.decodeFromString<League>(leagueJson ?: "")
        LeagueScreen(league, goToStockViewer)
    }
}

@Composable
fun LeagueScreen(
    league: League,
    goToStockViewer: (String) -> Unit
) {
    val viewModel: LeagueViewModel = viewModel<LeagueViewModel>()
    val leaderboard = viewModel.init(league)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = league.name,
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                modifier = Modifier
                    .padding(16.dp)
            )
            if (league.endDate != null ) {
                Text(
                    text = "Ending on: ${league.endDate}"
                )
            }
            IconButton(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .padding(end = 8.dp),
                onClick = { }
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "League Settings",
                    tint = Color.DarkGray
                )
            }
        }
        TabRow(selectedTabIndex = viewModel.selectedTab) {
            Tab(
                selected = viewModel.selectedTab == 0,
                onClick = { viewModel.selectPersonal() },
                text = { Text("My Performance")}
            )
            Tab(
                selected = viewModel.selectedTab == 1,
                onClick = { viewModel.selectShared() },
                text = { Text("Leaderboard") }
            )
        }
        when (viewModel.selectedTab) {
            0 -> Portfolio(viewModel, goToStockViewer)
            1 -> LeaderboardComposable(viewModel, leaderboard)
        }
    }
}

