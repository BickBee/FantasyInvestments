package com.example.fantasystocks.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fantasystocks.classes.League
import com.example.fantasystocks.database.SupabaseClient
import com.example.fantasystocks.ui.theme.InvalidRed
import com.example.fantasystocks.ui.viewmodels.LeagueSettingsViewModel
import com.example.fantasystocks.ui.viewmodels.LeagueSettingsViewModelFactory
import com.example.fantasystocks.ui.viewmodels.cashToString
import com.example.fantasystocks.ui.viewmodels.localDateToLong
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LeagueSettings(val leagueJson: String)

fun NavGraphBuilder.leagueSettingsViewer(goToHomeScreen: () -> Unit) {
    composable<LeagueSettings> { backStackEntry ->
        val leagueJson = backStackEntry.arguments?.getString("leagueJson")
        val json = Json { allowStructuredMapKeys = true }
        val league = json.decodeFromString<League>(leagueJson ?: "")
        LeagueSettings(league, goToHomeScreen)
    }
}

@Composable
fun LeagueSettings(
    league: League,
    goToHomeScreen: () -> Unit
) {
    val viewModel: LeagueSettingsViewModel = viewModel(
        factory = LeagueSettingsViewModelFactory(league)
    )
    val updatedLeague by viewModel.leagueState.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }

    val isLoading by viewModel.userSearchLoading.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val players by viewModel.players.collectAsState()

    LaunchedEffect(error) {
        error?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
        viewModel.resetError()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            modifier = Modifier.padding(32.dp)
        )

        LeagueSettingsRow(
            icon = { Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit name") },
            title = "League Name",
            onClick = { viewModel.editLeagueName() },
            buttonText = { Text(updatedLeague.name, maxLines = 1) }
        )

        LeagueSettingsRow(
            icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Add/Remove Players") },
            title = "Players",
            onClick = { viewModel.editPlayers() },
            buttonText = { Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Go") }
        )

        LeagueSettingsRow(
            icon = { Icon(imageVector = Icons.Filled.DateRange, contentDescription = "Start Date") },
            title = "Start Date",
            onClick = { viewModel.openStartDate() },
            buttonText = { Text(updatedLeague.startDate.toString(), maxLines = 1) }
        )

        LeagueSettingsRow(
            icon = { Icon(imageVector = Icons.Filled.DateRange, contentDescription = "End Date") },
            title = "End Date",
            onClick = { viewModel.openEndDate() },
            buttonText = { Text(updatedLeague.endDate.toString(), maxLines = 1) }
        )

        LeagueSettingsRow(
            icon = { Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Leave Group") },
            title = "Leave Group",
            onClick = { viewModel.openLeaveGroup() },
            buttonText = { Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Leave group") }
        )
    }

    if (viewModel.isLeagueNameShown) {
        EditDialog(
            onDismissRequest = { viewModel.closeLeagueName() },
            onConfirmRequest = {
                viewModel.updateLeagueName(viewModel.newLeagueName)
                viewModel.closeLeagueName()
            },
            title = "Change League Name",
            editFields = {
                OutlinedTextField(
                    value = viewModel.newLeagueName,
                    onValueChange = { viewModel.editNewLeagueName(it)},
                    shape = RoundedCornerShape(25.dp),
                    label = { Text("League Name") },
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            isConfirmEnabled = {
                viewModel.newLeagueName != viewModel.league.name && viewModel.newLeagueName.isNotBlank()
            }
        )
    }
    if (viewModel.isPlayersShown) {
        EditDialog(
            onDismissRequest = { viewModel.closePlayers() },
            onConfirmRequest = { viewModel.closePlayers() },
            title = "Add/Remove Players",
            hasSearch = true,
            onPlayerSearch = { viewModel.openPlayerSearch() },
            onClosePlayerSearch = { viewModel.closePlayerSearch() },
            editFields = {
                LazyColumn {
                    items(players) { player ->
                        val isCurrent = SupabaseClient.getCurrentUID() == player.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            shape = RoundedCornerShape(25.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = if (isCurrent)
                                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                            else
                                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Player profile picture"
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(player.name)
                                }
                                if (SupabaseClient.getCurrentUID() != player.id) {
                                    IconButton(
                                        onClick = { viewModel.removePlayer(player.id) },
                                        Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Clear,
                                            contentDescription = "Remove Player",
                                            tint = InvalidRed
                                        )
                                    }
                                } else {
                                    Text("You")
                                }
                            }
                        }
                    }
                }
                if (viewModel.isPlayerSearch) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            if (username.isNotEmpty()) {
                                viewModel.searchUsers(it)
                            } else {
                                viewModel.clearSearch()
                            }
                        },
                        shape = RoundedCornerShape(25.dp),
                        label = { Text("Search by username") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (searchResults.isNotEmpty()) {
                    Text(
                        text = "Search Results",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        itemsIndexed(searchResults) { idx, result ->
                            fun onClick() {
                                username = ""
                                viewModel.openNewPlayerCash()
                                viewModel.updateCashIsOpenFor(result)
                                viewModel.clearSearch()
                            }
                            if (idx == searchResults.lastIndex) {
                                UserSearchCard(user = result, onClick = { onClick() }, padding = 4)
                            } else {
                                UserSearchCard(user = result, onClick = { onClick() },)
                            }
                        }
                    }
                } else if (username.isNotEmpty()) {
                    Text("No users found")
                }
            },
            isConfirmEnabled = { false }
        )
    }

    if (viewModel.isNewPlayerCashOpen) {
        UpdateCash(
            onDismiss = { viewModel.closeNewPlayerCash() },
            onConfirm = {
                viewModel.closeNewPlayerCash()
                viewModel.addPlayerToLeague()
            },
            title = "Change ${viewModel.cashIsOpenFor?.username}'s starting cash",
            value = cashToString(viewModel.newPlayerCash),
            onValueChange = { viewModel.updateNewPlayerCash(it)}
        )
    }

    if (viewModel.isStartShown) {
        DatePickerModal(
            dateState = localDateToLong(updatedLeague.startDate),
            onDateSelected = { viewModel.updateStartDate(it) },
            onDismiss = { viewModel.closeStartDate() },
            end = localDateToLong(updatedLeague.endDate)
        )
    }

    if (viewModel.isEndShown) {
        DatePickerModal(
            dateState = localDateToLong(updatedLeague.endDate),
            onDateSelected = { viewModel.updateEndDate(it) },
            onDismiss = { viewModel.closeEndDate() },
            start = localDateToLong(updatedLeague.startDate),
        )
    }

    if (viewModel.isLeaveGroup) {
        EditDialog(
            onDismissRequest = { viewModel.closeLeaveGroup() },
            onConfirmRequest = {
                viewModel.closeLeaveGroup()
                viewModel.leaveGroup()
                goToHomeScreen()
            },
            title = "Are you sure you want to leave?",
            editFields = {},
            isConfirmEnabled = { true }
        )
    }
}

@Composable
fun LeagueSettingsRow(
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit,
    buttonText: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(0.6f),
                contentAlignment = Alignment.Center
            ) { icon() }

            Box(
                modifier = Modifier.weight(1.5f),
                contentAlignment = Alignment.CenterStart
            ) { Text(title) }

            Box(
                modifier = Modifier.weight(2f).padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) { buttonText() }
        }
    }
}

@Composable
fun EditDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,
    title: String,
    hasSearch: Boolean = false,
    onPlayerSearch: () -> Unit = {},
    onClosePlayerSearch: () -> Unit = {},
    editFields: @Composable () -> Unit,
    isConfirmEnabled: () -> Boolean
) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            elevation = CardDefaults.cardElevation(20.dp),
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(25.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (hasSearch) {
                    var opened by remember { mutableStateOf(false) }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!opened) {
                            IconButton(
                                onClick = {
                                    opened = true
                                    onPlayerSearch()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add players"
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    opened = false
                                    onClosePlayerSearch()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close player search"
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                editFields()
                if (!hasSearch) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ElevatedButton(
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.elevatedButtonColors(containerColor = InvalidRed),
                            onClick = { onDismissRequest() }
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        ElevatedButton(
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                            onClick = { onConfirmRequest() },
                            enabled = isConfirmEnabled()
                        ) {
                            Text(
                                text = "Confirm",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
