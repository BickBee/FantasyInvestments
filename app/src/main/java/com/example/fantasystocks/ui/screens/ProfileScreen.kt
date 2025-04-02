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
import com.example.fantasystocks.ui.components.AvatarSelector
import com.example.fantasystocks.ui.components.UserAvatar
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
    val userSettings by profileViewModel.userSettings.collectAsState()
    
    // State for username editing and avatar selection
    var isEditingUsername by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var showAvatarSelector by remember { mutableStateOf(false) }
    
    // Loading state for the entire profile screen
    var isPageLoading by remember { mutableStateOf(true) }
    
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
    
    // Load all necessary profile data
    LaunchedEffect(Unit) {
        isPageLoading = true
        profileViewModel.loadUserProfile()
        profileViewModel.loadUserSettings()
        // Wait for both profile and settings to load
        isPageLoading = false
    }
    
    // Update loading state based on profile and settings loading
    LaunchedEffect(userProfile, userSettings) {
        isPageLoading = userProfile.username.isEmpty() || userSettings == null
    }
    
    // Set up a non-null version of userSettings for use in the UI
    val settings = userSettings // Get local copy to avoid multiple collections
    
    if (showAvatarSelector && settings != null) {
        AvatarSelector(
            selectedAvatarId = settings.avatar_id,
            onSelectAvatar = { newAvatarId -> 
                profileViewModel.updateUserSettings(
                    settings.dark_mode,
                    settings.notification_enabled,
                    newAvatarId
                )
            },
            onDismiss = { showAvatarSelector = false }
        )
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Loading state for the entire screen
        if (isPageLoading || settings == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading profile...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Now settings is guaranteed to be non-null here
            // Content when loaded
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // User avatar
                    UserAvatar(
                        avatarId = settings.avatar_id,
                        username = userProfile.username,
                        size = 90,
                        onClick = { showAvatarSelector = true }
                    )
                    
                    TextButton(onClick = { showAvatarSelector = true }) {
                        Text("Change Avatar")
                    }
                }
                
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
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
                        }
                    }
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Settings", 
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            HorizontalDivider()
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Dark Mode")
                                
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Switch(
                                        checked = settings.dark_mode,
                                        onCheckedChange = { newDarkMode -> 
                                            profileViewModel.updateUserSettings(
                                                newDarkMode, 
                                                settings.notification_enabled,
                                                settings.avatar_id
                                            )
                                        }
                                    )
                                }
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Notifications")
                                
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Switch(
                                        checked = settings.notification_enabled,
                                        onCheckedChange = { newNotificationValue -> 
                                            profileViewModel.updateUserSettings(
                                                settings.dark_mode,
                                                newNotificationValue,
                                                settings.avatar_id
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = onNavigateToFriendsList,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Friends List")
                    }
                }
                
                item {
                    Button(
                        onClick = onNavigateToChangePassword,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Password")
                    }
                }
                
                item {
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                item {
                    // Show spinner for operations like updating username
                    if (isLoading && !isPageLoading) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
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
    
    // Add a dedicated loading state for the entire friends list screen
    var isPageLoading by remember { mutableStateOf(true) }
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    
    // Load data when the screen is first shown and manage the loading state
    LaunchedEffect(Unit) {
        isPageLoading = true
        profileViewModel.loadFriends()
        profileViewModel.loadFriendRequests()
        // Add a small delay to ensure complete loading and avoid flicker
        delay(500)
        isPageLoading = false
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
                text = { 
                    Text(if (isPageLoading) "Friends" else "Friends (${friends.size})")
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { 
                    Text(if (isPageLoading) "Requests" else "Requests (${incomingFriendRequests.size + outgoingFriendRequests.size})")
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Show a full-page loading state while the data is loading
        if (isPageLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading friends...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (isLoading) {
            // Show a loading indicator for operations like accepting/rejecting requests
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
                    avatarId = friend.avatarId,
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
                    avatarId = friend.avatarId,
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
    avatarId: Int = 0,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Add avatar to the friend request item
                UserAvatar(
                    avatarId = avatarId,
                    username = username,
                    size = 40
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
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
            // Friend avatar with the correct avatarId
            UserAvatar(
                username = friend.username,
                avatarId = friend.avatarId,
                size = 50
            )
            
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