package com.example.fantasystocks.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fantasystocks.classes.Leaderboard
import com.example.fantasystocks.database.SupabaseClient
import com.example.fantasystocks.ui.leaderboard.LeaderboardComposable
import com.example.fantasystocks.ui.portfolio.Portfolio
import com.example.fantasystocks.ui.viewmodels.LeagueViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class LeagueScreen(val leagueId: Int)

fun NavGraphBuilder.leagueScreenViewer(
    goToStockViewer: (String) -> Unit,
    goToLeagueSettingsViewer: (String) -> Unit
) {
    composable<LeagueScreen> { backStackEntry ->
        val leagueId = backStackEntry.arguments?.getInt("leagueId") ?: -1
        LeagueScreen(leagueId, goToStockViewer, goToLeagueSettingsViewer)
    }
}

@Composable
fun LeagueScreen(
    leagueId: Int,
    goToStockViewer: (String) -> Unit,
    goToLeagueSettingsViewer: (String) -> Unit
) {
    val viewModel: LeagueViewModel = viewModel<LeagueViewModel>()
    val league by viewModel.league.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val leaderboard = league?.let { Leaderboard(it) }

    val currentPlayer = league?.getCurrentPlayer(SupabaseClient.getCurrentUID()!!)

    LaunchedEffect(leagueId) {
        viewModel.fetchLeague(leagueId)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (error != null || league == null) {
        Text("ERROR: $error")
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = league!!.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    modifier = Modifier
                        .padding(16.dp)
                )
                if (league!!.endDate != null) {
                    Text(
                        text = "Ending on: ${league!!.endDate}"
                    )
                }
                IconButton(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .padding(end = 8.dp),
                    onClick = {
                        val json = Json { allowStructuredMapKeys = true }
                        goToLeagueSettingsViewer(json.encodeToString(league))
                    }
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
                    text = { Text("My Performance") }
                )
                Tab(
                    selected = viewModel.selectedTab == 1,
                    onClick = { viewModel.selectShared() },
                    text = { Text("Leaderboard") }
                )
            }
            when (viewModel.selectedTab) {
                0 -> Portfolio(leagueId, currentPlayer, viewModel, goToStockViewer)
                1 -> LeaderboardComposable(viewModel, leaderboard)
            }
        }
        Box(
            modifier = Modifier.padding(16.dp).fillMaxSize()
        ) {
            FloatingActionButton(
                onClick = { /* TODO */},
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(125.dp),
                shape = RoundedCornerShape(25.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "Buy/Sell",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}
