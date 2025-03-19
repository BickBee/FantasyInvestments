package com.example.fantasystocks.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fantasystocks.FriendsList
import com.example.fantasystocks.models.Friend
import com.example.fantasystocks.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
object ProfileDestination

// Match the pattern of other screens
fun NavGraphBuilder.profileDestination(
    onNavigateToFriendsList: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onSignOut: () -> Unit
) {
    composable<ProfileDestination> {
        ProfileScreen(
            onNavigateToFriendsList = onNavigateToFriendsList,
            onNavigateToChangePassword = onNavigateToChangePassword,
            onSignOut = onSignOut
        )
    }
    
    composable<FriendsList> {
        FriendsListScreen()
    }
}

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onNavigateToFriendsList: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()
    val isSuccess by profileViewModel.isSuccess.collectAsState()
    val settings = profileViewModel.userSettings.collectAsState().value
    
    // State for username editing
    var isEditingUsername by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    
    // Update newUsername when profile loads
    LaunchedEffect(userProfile.username) {
        if (userProfile.username.isNotEmpty()) {
            newUsername = userProfile.username
        }
    }
    
    // Handle success/error states
    LaunchedEffect(isSuccess, errorMessage) {
        if (isSuccess == true) {
            isEditingUsername = false
            profileViewModel.clearErrorsAndSuccess()
        }
        
        if (errorMessage != null) {
            usernameError = errorMessage
        } else {
            usernameError = null
        }
    }
    
    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
        profileViewModel.loadUserSettings()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Profile",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            // User avatar placeholder
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (userProfile.username.isNotEmpty()) 
                               userProfile.username.take(1).uppercase() 
                               else "U",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Account Information", 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    HorizontalDivider()
                    
                    Text("Email: ${userProfile.email}")
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isEditingUsername) {
                            OutlinedTextField(
                                value = newUsername,
                                onValueChange = { value -> 
                                    newUsername = value 
                                    // Basic validation
                                    if (value.length < 3) {
                                        usernameError = "Username must be at least 3 characters long"
                                    } else if (!value.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                                        usernameError = "Username can only contain letters, numbers, and underscores"
                                    } else {
                                        usernameError = null
                                    }
                                },
                                label = { Text("Username") },
                                modifier = Modifier.weight(1f),
                                isError = usernameError != null,
                                supportingText = { 
                                    if (usernameError != null) {
                                        Text(
                                            text = usernameError!!,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            )
                            
                            IconButton(
                                onClick = {
                                    if (usernameError == null && newUsername != userProfile.username) {
                                        profileViewModel.updateUsername(newUsername)
                                    } else if (newUsername == userProfile.username) {
                                        isEditingUsername = false
                                    }
                                },
                                enabled = usernameError == null
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save username"
                                )
                            }
                            
                            IconButton(
                                onClick = {
                                    newUsername = userProfile.username
                                    isEditingUsername = false
                                    usernameError = null
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Cancel"
                                )
                            }
                        } else {
                            Text(
                                text = "Username: ${userProfile.username}",
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(
                                onClick = { isEditingUsername = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit username"
                                )
                            }
                        }
                    }
                    
                    if (settings != null) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Text(
                            text = "Settings", 
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Dark Mode")
                            Switch(
                                checked = settings.dark_mode,
                                onCheckedChange = { 
                                    profileViewModel.updateUserSettings(
                                        it, 
                                        settings.notification_enabled
                                    )
                                }
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Notifications")
                            Switch(
                                checked = settings.notification_enabled,
                                onCheckedChange = { 
                                    profileViewModel.updateUserSettings(
                                        settings.dark_mode,
                                        it
                                    )
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onNavigateToFriendsList,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Friends List")
            }
            
            Button(
                onClick = onNavigateToChangePassword,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Change Password")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Sign Out")
            }
        }
    }
}

@Composable
fun FriendsListScreen(
    profileViewModel: ProfileViewModel = viewModel()
) {
    val friends by profileViewModel.friends.collectAsState()
    val incomingFriendRequests by profileViewModel.incomingFriendRequests.collectAsState()
    val outgoingFriendRequests by profileViewModel.outgoingFriendRequests.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        profileViewModel.loadFriends()
        profileViewModel.loadFriendRequests()
    }
    
    if (showAddFriendDialog) {
        AddFriendDialog(
            onDismiss = { showAddFriendDialog = false },
            onAddFriend = { username ->
                profileViewModel.sendFriendRequest(username)
                showAddFriendDialog = false
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Friends",
                style = MaterialTheme.typography.headlineMedium
            )
            
            IconButton(onClick = { showAddFriendDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Friend"
                )
            }
        }
        
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Friends (${friends.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Requests (${incomingFriendRequests.size + outgoingFriendRequests.size})") }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            when (selectedTab) {
                0 -> {
                    if (friends.isEmpty()) {
                        EmptyState(
                            message = "You don't have any friends yet. Add some friends to connect with them!"
                        )
                    } else {
                        FriendsList(
                            friends = friends,
                            onRemoveFriend = { friendId ->
                                profileViewModel.removeFriend(friendId)
                            }
                        )
                    }
                }
                1 -> {
                    if (incomingFriendRequests.isEmpty() && outgoingFriendRequests.isEmpty()) {
                        EmptyState(
                            message = "No pending friend requests."
                        )
                    } else {
                        FriendRequestsTab(
                            onAcceptRequest = { friendId ->
                                profileViewModel.acceptFriendRequest(friendId)
                            },
                            onRejectRequest = { friendId ->
                                profileViewModel.rejectFriendRequest(friendId)
                            },
                            onCancelRequest = { friendId ->
                                profileViewModel.cancelFriendRequest(friendId)
                            },
                            profileViewModel = profileViewModel
                        )
                    }
                }
            }
        }
        
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onAddFriend: (String) -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var selectedUsername by remember { mutableStateOf<String?>(null) }
    val searchResults by profileViewModel.searchResults.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val isSuccess by profileViewModel.isSuccess.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Clear search results when dialog is dismissed
    LaunchedEffect(Unit) {
        profileViewModel.clearSearchResults()
    }
    
    // Handle success state
    LaunchedEffect(isSuccess) {
        if (isSuccess == true) {
            delay(1000) // Show success message briefly
            onDismiss()
        }
    }
    
    AlertDialog(
        onDismissRequest = {
            keyboardController?.hide()
            onDismiss()
        },
        title = { Text("Add Friend") },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        selectedUsername = null
                        if (it.length >= 3) {
                            profileViewModel.searchUsers(it)
                        } else {
                            profileViewModel.clearSearchResults()
                        }
                    },
                    label = { Text("Search by username") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        } else if (username.length > 0 && username.length < 3) {
                            Text("Enter at least 3 characters to search", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (searchResults.isNotEmpty()) {
                    Text(
                        text = "Search Results",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(searchResults) { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedUsername = result.username
                                        username = result.username
                                        keyboardController?.hide()
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (selectedUsername == result.username) 
                                        Icons.Default.Check 
                                    else 
                                        Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (selectedUsername == result.username) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = result.username,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (selectedUsername == result.username) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }

                            HorizontalDivider()
                        }
                    }
                } else if (username.length >= 3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No users found",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    keyboardController?.hide()
                    selectedUsername?.let { onAddFriend(it) }
                },
                enabled = selectedUsername != null && !isLoading && errorMessage == null,
                modifier = Modifier.width(100.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Friend")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    keyboardController?.hide()
                    onDismiss()
                },
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FriendsList(
    friends: List<Friend>,
    onRemoveFriend: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(friends) { friend ->
            FriendItem(
                friend = friend,
                onRemove = { onRemoveFriend(friend.id) }
            )
        }
    }
}

@Composable
fun FriendRequestsTab(
    onAcceptRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onCancelRequest: (String) -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val incomingFriendRequests by profileViewModel.incomingFriendRequests.collectAsState()
    val outgoingFriendRequests by profileViewModel.outgoingFriendRequests.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Incoming Requests Section
        if (incomingFriendRequests.isNotEmpty()) {
            Text(
                text = "Incoming Requests",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            incomingFriendRequests.forEach { friend ->
                FriendRequestItem(
                    username = friend.username,
                    onAccept = { onAcceptRequest(friend.id) },
                    onReject = { onRejectRequest(friend.id) },
                    isIncoming = true
                )
            }
        }

        // Outgoing Requests Section
        if (outgoingFriendRequests.isNotEmpty()) {
            Text(
                text = "Outgoing Requests",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            outgoingFriendRequests.forEach { friend ->
                FriendRequestItem(
                    username = friend.username,
                    onCancel = { onCancelRequest(friend.id) },
                    isIncoming = false
                )
            }
        }

        if (incomingFriendRequests.isEmpty() && outgoingFriendRequests.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No pending friend requests",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FriendRequestItem(
    username: String,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    isIncoming: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = username,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            
            if (isIncoming) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAccept?.invoke() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Accept")
                    }
                    Button(
                        onClick = { onReject?.invoke() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Reject")
                    }
                }
            } else {
                Button(
                    onClick = { onCancel?.invoke() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun FriendItem(
    friend: Friend,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Friend avatar
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.secondary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = friend.username.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = friend.username,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            
            Button(
                onClick = onRemove,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Remove")
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}