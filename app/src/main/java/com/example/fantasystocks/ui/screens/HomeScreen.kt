package com.example.fantasystocks.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fantasystocks.classes.League
import com.example.fantasystocks.ui.viewmodels.HomeViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
object Home

/*
fun NavGraphBuilder.homeDestination(goToPortfolioViewer: (String) -> Unit) {
    composable<Home> { HomeScreen(goToPortfolioViewer) }
}
 */

fun NavGraphBuilder.homeDestination(goToLeagueScreen: (Int) -> Unit) {
    composable<Home> { HomeScreen(goToLeagueScreen) }
}

@Composable
fun HomeScreen(
    //goToPortfolioViewer: (String) -> Unit,
    goToLeagueScreen: (Int) -> Unit
) {
    val viewModel: HomeViewModel = viewModel<HomeViewModel>()
    val leagueCreationResult by viewModel.leagueCreationResult.collectAsState()
    val leagues by viewModel.leagues.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getUsersLeagues()
    }

    LaunchedEffect(leagueCreationResult) {
        leagueCreationResult?.let { leagueId ->
            goToLeagueScreen(leagueId)
            viewModel.resetLeagueCreationResult()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        
        item {
            Button(
                onClick = { viewModel.openLeagueDialog() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Text("Start New League")
            }
        }

        item {
            Text(
                text = "Your Leagues",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        if (viewModel.initLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(leagues) { league ->
                SessionItem(
                    league = league,
                    subtitle = "Join the competition",
                    goToLeagueScreen = goToLeagueScreen
                )
            }
            /* TODO separate ongoing with upcoming sessions
            item {
                Text(
                    text = "Upcoming Sessions",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
             */
        }


    }
    if (viewModel.isDialogShown) {
        LeagueDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeLeagueDialog() },
            onConfirm = { }
        )
    }
}

@Composable
private fun SessionItem(
    league: League,
    subtitle: String,
    goToLeagueScreen: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clickable {
                    try {
                        val leagueId = requireNotNull(league.id) { "CAN NOT CLICK, LEAGUE ID NULL" }
                        goToLeagueScreen(leagueId)
                    } catch (e: Exception) {
                        println(e)
                    }
                },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = league.name, style = MaterialTheme.typography.titleMedium)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            // TODO change the date format from 2025-03-06 to Mar 6, 2025
            Text(text = league.startDate.toString(), style = MaterialTheme.typography.bodySmall)
        }
    }
}