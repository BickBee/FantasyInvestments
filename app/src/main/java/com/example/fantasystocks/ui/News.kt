package com.example.fantasystocks.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object News

fun NavGraphBuilder.newsDestination() {
    composable<News> { NewsScreen() }
}

@Composable
fun NewsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Recommended for You",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

//        item {
//            NewsCard(
//                title = "Investment Strategies 101",
//                description = "Beginner-friendly guide to investing",
//                imageRes = R.drawable.investment_graph
//            )
//        }
//
//        item {
//            NewsCard(
//                title = "Virtual Stock Market",
//                description = "Compete with friends in simulated trading",
//                imageRes = R.drawable.chess_strategy
//            )
//        }

        item {
            Text(
                text = "Upcoming Events",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        item {
            EventItem(
                title = "Stock Market Challenge",
                subtitle = "Join the competition",
                date = "Oct 15, 2021"
            )
            EventItem(
                title = "Investment Workshop",
                subtitle = "Enhance your knowledge",
                date = "Nov 1, 2021"
            )
        }
    }
}

@Composable
private fun NewsCard(
    title: String,
    description: String,
    imageRes: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun EventItem(
    title: String,
    subtitle: String,
    date: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = date, style = MaterialTheme.typography.bodySmall)
        }
    }
}