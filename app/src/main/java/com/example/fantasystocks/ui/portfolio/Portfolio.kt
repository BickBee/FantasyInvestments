package com.example.fantasystocks.ui.portfolio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fantasystocks.classes.Player
import com.example.fantasystocks.ui.components.StockGraph
import com.example.fantasystocks.ui.viewmodels.LeagueViewModel
import com.example.fantasystocks.ui.viewmodels.doubleMoneyToString


@Composable
fun Portfolio(
    viewModel: LeagueViewModel,
    goToStockViewer: (String) -> Unit
) {
    val player = viewModel.portInit()
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = doubleMoneyToString(player.getTotalValue()),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 22.dp)
        )
        Text(
            text = "-7.9% ($322.18)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.Red,
            modifier = Modifier
                .padding(vertical = 4.dp)
        )
        StockGraph(viewModel.portValuesInit())
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Portfolio",
                    fontWeight = if (viewModel.personalPerformanceTab == 0) FontWeight.Bold
                                 else FontWeight.Normal,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { viewModel.selectPortfolio() },
                    color = if (viewModel.personalPerformanceTab == 0) Color.Black else Color.Gray,
                )
                Spacer(Modifier.width(24.dp))
                Text(
                    text = "Activity",
                    fontWeight = if (viewModel.personalPerformanceTab == 1) FontWeight.Bold
                                 else FontWeight.Normal,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { viewModel.selectActivity() },
                    color = if (viewModel.personalPerformanceTab == 1) Color.Black else Color.Gray,
                )
            }
            HorizontalDivider(thickness = 1.dp)
            Row() {
                AnimatedVisibility(
                    visible = viewModel.personalPerformanceTab == 0,
                    enter = slideInHorizontally(
                        initialOffsetX = { -it }, // slide in from the left
                        animationSpec = tween(durationMillis = 150, easing = EaseIn) // Fast and smooth
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { -it }, // slide out to the left
                        animationSpec = tween(durationMillis = 150, easing = EaseOut)
                    )
                ) {
                    PortfolioComposable(player, goToStockViewer)
                }
                AnimatedVisibility(
                    visible = viewModel.personalPerformanceTab == 1,
                    enter = slideInHorizontally(
                        initialOffsetX = { it }, // slide in from the right
                        animationSpec = tween(durationMillis = 150, easing = EaseIn)
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it }, // slide out to the right
                        animationSpec = tween(durationMillis = 150, easing = EaseOut)
                    )
                ) {
                    Activity(viewModel)
                }
            }
        }

    }
}

@Composable
fun PortfolioComposable(
    player: Player,
    goToStockViewer: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(player.getPortfolio(sortBy = Player.SortBy.VALUE)) { stockPair ->
            Card(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clickable { goToStockViewer(stockPair.first.ticker) },
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stockPair.first.name,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = doubleMoneyToString(stockPair.first.price * stockPair.second),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stockPair.first.ticker,
                        fontWeight = FontWeight.Light,
                        style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "+2.98%",
                        fontWeight = FontWeight.Light,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun Activity(
    viewModel: LeagueViewModel
) {
    val txns = viewModel.activityInit()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(txns) { txn ->
            Card(
                modifier = Modifier
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = txn.type.toString(),
                        fontWeight = FontWeight.Light,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = txn.date.toString(),
                        fontWeight = FontWeight.Light,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Text(
                    text = "${txn.amount} ${txn.stock}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )
            }
        }
    }
}