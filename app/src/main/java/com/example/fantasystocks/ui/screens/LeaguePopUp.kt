package com.example.fantasystocks.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.TextField
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.example.fantasystocks.ui.theme.InvalidRed
import com.example.fantasystocks.ui.viewmodels.convertMillisToDate
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            value = viewModel.cashToString(),
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
                AddPlayersSection(viewModel) // composable under
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
}

@Composable
fun AddPlayersSection(
    viewModel: HomeViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp, 2.dp, 15.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Add Players",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            IconButton(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape),
                onClick = { /*viewModel.openPlayerField()*/ },
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Player",
                    tint = Color.DarkGray
                )
            }
        }
        viewModel.players.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 8.dp, 0.dp, 0.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = MaterialTheme.colorScheme.background),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(it.name,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                IconButton(
                    modifier = Modifier
                        .size(22.dp)
                        .padding(2.dp),
                    onClick = { viewModel.removePlayer(it) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Remove",
                    )
                }
            }
        }
        if (viewModel.addPlayerField) {
            /*
            TextField(
                value = viewModel.playerName,
                onValueChange = { viewModel.updatePlayerName(it) },
                shape = RoundedCornerShape(25.dp),
                label = { Text("Player Name") },
                maxLines = 1,
                trailingIcon = {
                    Row (
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Confirm icon (Checkmark)
                        IconButton(
                            modifier = Modifier
                                .size(22.dp),
                            onClick = { viewModel.addPlayer() }) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = "Confirm")
                        }
                        // Delete icon (Trash)
                        IconButton(
                            modifier = Modifier
                                .size(22.dp),
                            onClick = { viewModel.deletePlayerName() }) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
             */
            val searchText by viewModel.searchText.collectAsState()
            val users by viewModel.users.collectAsState()
            val isSearching by viewModel.isSearching.collectAsState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                TextField(
                    value = searchText,
                    onValueChange = viewModel::onPlayerSearch,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text= "Search") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        items(users) { user ->
                            Text(
                                text = user.username,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
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
                    end = false,
                    onDateSelected = { viewModel.updateStartDate(it) },
                    onDismiss = { showModal = false }
                )
            } else if (labelText == "End") {
                DatePickerModal(
                    dateState = viewModel.endDate,
                    end = true,
                    onDateSelected = { viewModel.updateEndDate(it) },
                    onDismiss = { showModal = false }
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
class FutureSelectableDates: SelectableDates {
    private val now = java.time.LocalDate.now()
    private val dayStart = now.atTime(0, 0, 0, 0).toEpochSecond(ZoneOffset.UTC) * 1000

    @ExperimentalMaterial3Api
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
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
    end: Boolean,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateState,
        initialDisplayedMonthMillis = dateState,
        selectableDates = FutureSelectableDates()
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


