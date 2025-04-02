package com.example.fantasystocks.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fantasystocks.models.Friend
import com.example.fantasystocks.ui.components.UserAvatar
import com.example.fantasystocks.ui.theme.FantasyStocksTheme
import com.example.fantasystocks.ui.theme.ThemeManager
import com.example.fantasystocks.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    // Use ThemeManager to get current dark mode setting across the app
    val isDarkTheme by ThemeManager.isDarkTheme.collectAsState()
    
    // Get context for window modifications
    val view = LocalView.current
    val context = LocalContext.current
    
    // State tracking
    val incomingFriendRequests by profileViewModel.incomingFriendRequests.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    
    // Track if back navigation is safe
    var dataLoadFinished by remember { mutableStateOf(false) }
    
    // Special handling for empty state to avoid flicker
    var shouldShowEmptyState by remember { mutableStateOf(false) }
    
    // Wrap entire content in FantasyStocksTheme
    FantasyStocksTheme {
        // Now we can access MaterialTheme.colorScheme which will have the correct colors
        val backgroundColor = MaterialTheme.colorScheme.background
        
        // Apply system bars color immediately on composition to prevent flash
        SideEffect {
            val window = (context as? Activity)?.window
            window?.let {
                // Set both statusBarColor and navigationBarColor to appropriate theme color immediately
                val backgroundArgb = backgroundColor.toArgb()
                it.statusBarColor = backgroundArgb
                it.navigationBarColor = backgroundArgb
                
                // Set appearance to match dark/light theme
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = !isDarkTheme
                    isAppearanceLightNavigationBars = !isDarkTheme
                }
            }
        }
    
        // Ensure friend requests are loaded
        LaunchedEffect(Unit) {
            try {
                profileViewModel.loadFriendRequests()
                // Small delay to ensure data has loaded properly
                delay(600)
                dataLoadFinished = true
            } catch (e: Exception) {
                // Handle exception
            } finally {
                dataLoadFinished = true
            }
        }
        
        // Effect to update the empty state only after loading is complete
        LaunchedEffect(isLoading, incomingFriendRequests) {
            if (!isLoading && dataLoadFinished) {
                // Add a small delay before showing empty state to avoid flickering
                if (incomingFriendRequests.isEmpty()) {
                    delay(200) // Small delay to ensure we're truly done loading
                    shouldShowEmptyState = true
                } else {
                    shouldShowEmptyState = false
                }
            }
        }
    
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = backgroundColor
        ) {
            Scaffold(
                containerColor = backgroundColor,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Notifications",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { 
                                if (dataLoadFinished) {
                                    onNavigateBack()
                                } else {
                                    // Cancel any loading operations and navigate back
                                    profileViewModel.cancelLoading()
                                    onNavigateBack()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .padding(paddingValues)
                ) {
                    // Loading state - only show when actually loading or when data isn't fully loaded yet
                    if (isLoading || !dataLoadFinished) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    } else {
                        // Data is loaded - show content
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            // Always show the section header
                            item {
                                Text(
                                    text = "Friend Requests",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            // Display friend requests if we have any
                            if (incomingFriendRequests.isNotEmpty()) {
                                items(incomingFriendRequests) { request ->
                                    FriendRequestItem(
                                        request = request,
                                        onAccept = { profileViewModel.acceptFriendRequest(request.id) },
                                        onReject = { profileViewModel.rejectFriendRequest(request.id) }
                                    )
                                }
                            } else if (shouldShowEmptyState) {
                                // Empty state shown only after we confirmed data is loaded and empty
                                item {
                                    Text(
                                        text = "No new friend requests",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendRequestItem(
    request: Friend,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Friend avatar using our UserAvatar component - now properly pass the avatarId
                UserAvatar(
                    avatarId = request.avatarId,
                    username = request.username,
                    size = 50
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = request.username,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Sent you a friend request",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Decline")
                }

                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Accept")
                }
            }
        }
    }
}