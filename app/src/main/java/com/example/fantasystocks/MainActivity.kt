package com.example.fantasystocks

import android.app.ActionBar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fantasystocks.database.SupabaseClient
import com.example.fantasystocks.ui.Home
import com.example.fantasystocks.ui.News
import com.example.fantasystocks.ui.Stocks
import com.example.fantasystocks.ui.homeDestination
import com.example.fantasystocks.ui.newsDestination
import com.example.fantasystocks.ui.screens.LoginScreen
import com.example.fantasystocks.ui.stocksDestination
import com.example.fantasystocks.ui.theme.FantasyStocksTheme
import com.example.fantasystocks.ui.viewmodels.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks // NEEDED?
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.security.MessageDigest
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
            val navController = rememberNavController()

            FantasyStocksTheme {
                NavHost(
                    navController = navController,
                    startDestination = if (isAuthenticated == true) MainApp.route else "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate(MainApp.route) {
                                    // Clear the back stack to prevent returning to login
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(MainApp.route) {
                        MyApp()  // Navigate to MyApp() upon successful login
                    }
                }
            }
        }
    }
}

@Serializable
data class Profile(val name: String)

@Serializable
object FriendsList

@Serializable
object MainApp {
    const val route = "main_app"
}

// Define the ProfileScreen composable.
@Composable
fun ProfileScreen(
    profile: Profile,
    onNavigateToFriendsList: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile for ${profile.name}")
        Button(onClick = { onNavigateToFriendsList() }) {
            Text(text = "Go to Friends List")
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    // Your loading screen implementation
}

// Define the FriendsListScreen composable.
@Composable
fun FriendsListScreen(onNavigateToProfile: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Friends List")
        Button(onClick = { onNavigateToProfile() }) {
            Text("Go to Profile")
        }
    }
}


// Define the MyApp composable, including the `NavController` and `NavHost`.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp() {
    val navController = rememberNavController()
    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home, Home),
        NavItem("News", Icons.Default.Build, News),
        NavItem("Stocks", Icons.Default.Face, Stocks)
    )
    val baseRoutes = navItemList.map {item -> item.route.javaClass.toString().removePrefix("class ")}
    Scaffold(
        topBar = {
            val currentBackStackEntry = navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStackEntry.value?.destination?.route
            val isBaseRoute = baseRoutes.contains(currentRoute)

            TopAppBar(
                title = { Text(
                    text = "Fantasy Investments",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Background color
                    titleContentColor = MaterialTheme.colorScheme.onPrimary // Text color
                ),
                navigationIcon = {
                    if (!isBaseRoute) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(start = 8.dp) // Space from the left edge
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary // Icon color
                            )
                        }
                    } else null
                },
                actions = {
                    // You can add action icons here if needed
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val currentBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry.value?.destination?.route
                val isBaseRoute = baseRoutes.contains(currentRoute)

                navItemList.forEach { navItem ->
                    NavigationBarItem(
                        enabled = isBaseRoute,
                        selected = currentRoute == navItem.route.javaClass.toString().removePrefix("class "),
                        icon = { Icon(imageVector = navItem.icon, contentDescription = "Icon") },
                        label = { Text(navItem.label) },
                        onClick = {
                            navController.navigate(navItem.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->  // Pass innerPadding to avoid overlap
        NavHost(
            navController,
            startDestination = Home,
            modifier = Modifier.padding(innerPadding) // Apply padding
        ) {
            homeDestination()
            newsDestination()
            stocksDestination()
            composable<Profile> { backStackEntry ->
                val name = backStackEntry.arguments?.getString("name") ?: "Unknown"
                ProfileScreen(
                    profile = Profile(name),
                    onNavigateToFriendsList = {
                        navController.navigate(FriendsList)
                    }
                )
            }
            composable<FriendsList> {
                FriendsListScreen(
                    onNavigateToProfile = {
                        navController.navigate(Profile("Aisha Devi"))
                    }
                )
            }
        }
    }
}
