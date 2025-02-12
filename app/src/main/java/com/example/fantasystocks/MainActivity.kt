package com.example.fantasystocks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fantasystocks.ui.Home
import com.example.fantasystocks.ui.News
import com.example.fantasystocks.ui.Stocks
import com.example.fantasystocks.ui.homeDestination
import com.example.fantasystocks.ui.newsDestination
import com.example.fantasystocks.ui.stocksDestination
import com.example.fantasystocks.ui.theme.FantasyStocksTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FantasyStocksTheme {
                MyApp()
            }
        }
    }
}

@Serializable
data class Profile(val name: String)

@Serializable
object FriendsList

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
@Composable
fun MyApp() {
    val navController = rememberNavController()
    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home, Home),
        NavItem("News", Icons.Default.Build, News),
        NavItem("Stocks", Icons.Default.Face, Stocks)
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry.value?.destination?.route

                navItemList.forEach { navItem ->
                    NavigationBarItem(
                        selected = currentRoute == navItem.route.javaClass.toString().removePrefix("class "),
                        icon = { Icon(imageVector = navItem.icon, contentDescription = "Icon") },
                        label = { Text(navItem.label) },
                        onClick = {
                            navController.navigate(navItem.route)
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
