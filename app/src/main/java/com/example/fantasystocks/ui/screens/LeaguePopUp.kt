package com.example.fantasystocks.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.fantasystocks.ui.viewmodels.HomeViewModel
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.example.fantasystocks.classes.Player
import com.example.fantasystocks.database.SupabaseClient
import com.example.fantasystocks.models.UserInformation
import com.example.fantasystocks.ui.theme.InvalidRed
import com.example.fantasystocks.ui.viewmodels.cashToString
import com.example.fantasystocks.ui.viewmodels.convertMillisToDate
import com.example.fantasystocks.ui.viewmodels.doubleStringToMoneyString
import com.example.fantasystocks.ui.viewmodels.localToUTC
import java.time.ZoneOffset

@Composable
fun LeagueDialog(
    viewModel: HomeViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
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
                    .padding(20.dp, 40.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Create A New League",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                OutlinedTextField(
                    value = viewModel.leagueName,
                    onValueChange = { viewModel.updateLeagueName(it) },
                    shape = RoundedCornerShape(25.dp),
                    label = { Text("League Name") },
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DatePickerFieldToModal(
                        viewModel,
                        "Start",
                        modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    DatePickerFieldToModal(
                        viewModel,
                        "End",
                        modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Same Initial Cash")
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = viewModel.useCashForAll,
                        onCheckedChange = { viewModel.setCashForAll(it) }
                    )
                    if (viewModel.useCashForAll) {
                        Spacer(modifier = Modifier.width(16.dp))
                        OutlinedTextField(
                            value = cashToString(viewModel.cashForAll),
                            onValueChange = {
                                viewModel.updateCashForAll(it)
                            },
                            // only numbers keyboard comes up
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            maxLines = 1,
                            leadingIcon = { Text("$") },
                            label = { Text("Cash")},
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.End
                            )
                        )
                    }
                }
                HorizontalDivider(thickness = 2.dp)
                AddPlayersSection(viewModel)
                HorizontalDivider(thickness = 2.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ElevatedButton(
                        modifier = Modifier
                            .weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = InvalidRed),
                        onClick = { viewModel.closeLeagueDialog() }
                    ) {
                        Text(text = "Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    ElevatedButton(
                        modifier = Modifier
                            .weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                        onClick = {
                            viewModel.createLeague()
                        },
                        enabled = viewModel.leagueName.isNotBlank()
                    ) {
                        Text(text = "Confirm", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    if (viewModel.isCashOpen) {
        UpdateCash(
            onDismiss = { viewModel.closeCash() },
            onConfirm = {
                viewModel.updateSingleCash()
                viewModel.closeCash()
            },
            title = "Change ${viewModel.cashIsOpenFor}'s starting cash",
            value = cashToString(viewModel.singleCash),
            onValueChange = { viewModel.updateSingleCash(it) }
        )
    }
}

@Composable
fun AddPlayersSection(
    viewModel: HomeViewModel
) {
    var username by remember { mutableStateOf("") }

    val isLoading by viewModel.userSearchLoading.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val players by viewModel.players.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp, 2.dp, 15.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Add Players",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            if (viewModel.addPlayerField) {
                IconButton(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape),
                    onClick = { viewModel.closePlayerField() },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Add Player",
                        tint = Color.DarkGray
                    )
                }
            } else {
                IconButton(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape),
                    onClick = { viewModel.openPlayerField() },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Player",
                        tint = Color.DarkGray
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(players) { idx, player ->
                fun onCashClick() {
                    viewModel.openCash()
                    viewModel.updateCashIsOpenFor(player.name)
                    viewModel.updateSingleCash(player.cash.toInt())
                }
                if (idx == players.lastIndex) {
                    SelectedPlayerCard(player, viewModel, 16, { onCashClick() })
                } else {
                    SelectedPlayerCard(player, viewModel, onCashClick =  { onCashClick() })
                }
            }
        }
        if (viewModel.addPlayerField) {
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

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else if (searchResults.isNotEmpty()) {
                Text(
                    text = "Search Results",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    itemsIndexed(searchResults) { idx, result ->
                        fun onClick() {
                            username = ""
                            viewModel.addPlayerToLeague(result)
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

        }
    }
}

@Composable
fun UserSearchCard(
    user: UserInformation,
    onClick: () -> Unit,
    padding: Int = 0,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                top = 4.dp,
                start = 4.dp,
                end = 4.dp,
                bottom = padding.dp
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "User",
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(user.username)
        }
    }
}

@Composable
fun SelectedPlayerCard(
    player: Player,
    viewModel: HomeViewModel,
    padding: Int = 0,
    onCashClick: () -> Unit = {}
) {
    val isCurrent = SupabaseClient.getCurrentUID() == player.id
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 4.dp,
                start = 4.dp,
                end = 4.dp,
                bottom = padding.dp
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = if (isCurrent)
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        else
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1.9f)) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User",
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(player.name)
            }
            Box(modifier = Modifier.weight(1f)) {
                if (!viewModel.useCashForAll) {
                    Text(
                        text = "$" + doubleStringToMoneyString(player.cash.toInt().toString()),
                        modifier = Modifier
                            .clickable { onCashClick() }
                    )
                }
            }
            Box(modifier = Modifier.weight(0.3f)) {
                if (isCurrent) {
                    Text("You", maxLines = 1)
                } else {
                    IconButton(
                        modifier = Modifier.size(22.dp),
                        onClick = { viewModel.removePlayer(player) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove player"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateCash(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    value: String,
    onValueChange: (cash: String) -> Unit
) {
    EditDialog(
        onDismissRequest = { onDismiss() },
        onConfirmRequest = { onConfirm() },
        title = title,
        editFields = {
            OutlinedTextField(
                value = value,
                onValueChange = { onValueChange(it) },
                // only numbers keyboard comes up
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                maxLines = 1,
                leadingIcon = { Text("$") },
                label = { Text("Cash")},
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.End
                ),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        },
        isConfirmEnabled = { true }
    )
}


@Composable
fun DatePickerFieldToModal(
    viewModel: HomeViewModel,
    labelText: String,
    modifier: Modifier = Modifier
) {
    var selectedDate: Long? = when(labelText) {
        "Start" -> viewModel.startDate
        "End" -> viewModel.endDate
        else -> null
    }

    //var startDate by remember { mutableStateOf<Long?>() }
    var showModal by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate?.let { convertMillisToDate(it) } ?: "",
        onValueChange = { },
        label = { Text(labelText) },
        placeholder = { Text("MM/DD/YYYY") },
        maxLines = 1,
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Select date")
        },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(selectedDate) {
                awaitEachGesture {
                    // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                    // in the Initial pass to observe events before the text field consumes them
                    // in the Main pass.
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        showModal = true
                    }
                }
            }
    )

    if (showModal) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (labelText == "Start") {
                DatePickerModal(
                    dateState = viewModel.startDate,
                    onDateSelected = { viewModel.updateStartDate(it) },
                    onDismiss = { showModal = false },
                    end = viewModel.endDate
                )
            } else if (labelText == "End") {
                DatePickerModal(
                    dateState = viewModel.endDate,
                    onDateSelected = { viewModel.updateEndDate(it) },
                    onDismiss = { showModal = false },
                    start = viewModel.startDate
                )
            }
        } else {
            if (labelText == "Start") {
                DatePickerModalOld(
                    dateState = viewModel.startDate,
                    onDateSelected = { viewModel.updateStartDate(it) },
                    onDismiss = { showModal = false }
                )
            } else if (labelText == "End") {
                DatePickerModalOld(
                    dateState = viewModel.endDate,
                    onDateSelected = { viewModel.updateEndDate(it) },
                    onDismiss = { showModal = false }
                )
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
class FutureSelectableDates(private val start: Long?, private val end: Long?): SelectableDates {
    private val now = java.time.LocalDate.now()
    private val dayStart = now.atTime(0, 0, 0, 0).toEpochSecond(ZoneOffset.UTC) * 1000

    @ExperimentalMaterial3Api
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        if (start != null && end != null) {
            return utcTimeMillis > start && utcTimeMillis < end && utcTimeMillis >= dayStart
        } else if (start != null) {
            return utcTimeMillis > start && utcTimeMillis >= dayStart
        } else if (end != null) {
            return utcTimeMillis >= dayStart && utcTimeMillis < end
        }
        return utcTimeMillis >= dayStart
    }

    @ExperimentalMaterial3Api
    override fun isSelectableYear(year: Int): Boolean {
        return year >= now.year
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    dateState: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    start: Long? = null,
    end: Long? = null,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateState,
        initialDisplayedMonthMillis = dateState,
        selectableDates = FutureSelectableDates(start, end)
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
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
                    .padding(16.dp)
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    modifier = Modifier
                        .wrapContentSize()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                // time from date picker is in utc
                                onDateSelected(it + localToUTC())
                            }
                            onDismiss()
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}


// old calendar can select dates in the past
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModalOld(
    dateState: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateState,
        initialDisplayedMonthMillis = dateState,
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
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
                    .padding(16.dp)
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    modifier = Modifier
                        .wrapContentSize()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                onDateSelected(it)
                            }
                            onDismiss()
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}


