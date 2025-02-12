package com.example.fantasystocks.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object Home

fun NavGraphBuilder.homeDestination() {
    composable<Home> { HomeScreen() }
}

@Composable
fun HomeScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        
        item {
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text("Compete Now")
            }
        }
        
        item {
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Start New Session")
            }
        }

        item {
            Text(
                text = "Your Sessions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        item {
            SessionItem(
                title = "The bois",
                subtitle = "Join the competition",
                date = "Jan 15, 2025"
            )
        }

        item {
            Text(
                text = "Upcoming Sessions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        item {
            SessionItem(
                title = "Stock Market Challenge",
                subtitle = "Join the competition",
                date = "Feb 8, 2025"
            )
            SessionItem(
                title = "Investment Workshop",
                subtitle = "Enhance your knowledge",
                date = "Mar 1, 2025"
            )
        }
    }
}

@Composable
private fun SessionItem(
    title: String,
    subtitle: String,
    date: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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