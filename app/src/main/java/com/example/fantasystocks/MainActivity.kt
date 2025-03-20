package com.example.fantasystocks

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fantasystocks.ui.News
import com.example.fantasystocks.ui.newsDestination
import com.example.fantasystocks.ui.screens.AuthScreen
import com.example.fantasystocks.ui.screens.FriendsListScreen
import com.example.fantasystocks.ui.screens.Home
import com.example.fantasystocks.ui.screens.ProfileDestination
import com.example.fantasystocks.ui.screens.ProfileScreen
import com.example.fantasystocks.ui.screens.LeagueScreen
import com.example.fantasystocks.ui.screens.Stocks
import com.example.fantasystocks.ui.screens.authScreens
import com.example.fantasystocks.ui.screens.homeDestination
import com.example.fantasystocks.ui.screens.profileDestination
import com.example.fantasystocks.ui.screens.stocksDestination
import com.example.fantasystocks.ui.theme.FantasyStocksTheme
import com.example.fantasystocks.ui.viewmodels.AuthViewModel
import kotlinx.serialization.Serializable
import com.example.fantasystocks.ui.screens.stockViewer
import com.example.fantasystocks.ui.screens.Stock
import com.example.fantasystocks.ui.screens.leagueScreenViewer
import com.example.fantasystocks.ui.screens.portfolioViewer
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
            val navController = rememberNavController()
            val errorMessage by authViewModel.errorMessage.collectAsState()
            val context = LocalContext.current

            // Show toast for error messages
            LaunchedEffect(errorMessage) {
                errorMessage?.let { message ->
                    val shortMessage = if (message.contains(":")) {
                        message.substringBefore(":")
                    } else {
                        message.take(50) + if (message.length > 50) "..." else ""
                    }
                    Toast.makeText(context, shortMessage, Toast.LENGTH_SHORT).show()
                }
            }

            // Check auth state when the app starts
            LaunchedEffect(Unit) {
                authViewModel.checkAuthState()
            }

            FantasyStocksTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                NavHost(
                    navController = navController,
                    startDestination = if (isAuthenticated == true) MainApp.route else AuthScreen.Login.route
                ) {
                    // Auth flow with multiple screens
                    authScreens(
                        navController = navController,
                        authViewModel = authViewModel,
                        onLoginSuccess = {
                            navController.navigate(MainApp.route) {
                                popUpTo(AuthScreen.Login.route) { inclusive = true }
                            }
                        }
                    )

                    composable(MainApp.route) {
                        MyApp(
                            onSignOut = {
                                authViewModel.signOut()
                                navController.navigate(AuthScreen.Login.route) {
                                    popUpTo(MainApp.route) { inclusive = true }
                                }
                            },
                            onNavigateToChangePassword = {
                                navController.navigate(AuthScreen.ChangePassword.route)
                            },
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    }
}

@Serializable
data class Profile(val name: String = "User", val email: String = "user@example.com")

@Serializable
object FriendsList

@Serializable
object MainApp {
    const val route = "main_app"
}

// Define the MyApp composable, including the `NavController` and `NavHost`.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(
    onSignOut: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Create a remembered state to store the previous screen title
    val previousScreenTitle = remember { mutableStateOf("Fantasy Investments") }

    // Update the when statement
    val currentScreenTitle = when(currentRoute) {
        "com.example.fantasystocks.ui.screens.Home" -> "Home"
        "com.example.fantasystocks.ui.News" -> "News"
        "com.example.fantasystocks.ui.screens.Stocks" -> "Stocks"
        "com.example.fantasystocks.ui.screens.ProfileDestination" -> "Profile"
        else -> previousScreenTitle.value
    }

    // After determining the new title, update the remembered previous value
    LaunchedEffect(currentScreenTitle) {
        previousScreenTitle.value = currentScreenTitle
    }

    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home, Home),
        NavItem("News", Icons.Default.Build, News),
        NavItem("Stocks", Icons.Default.Face, Stocks),
        NavItem("Profile", Icons.Default.Person, ProfileDestination)
    )
    val baseRoutes = navItemList.map {item -> item.route.javaClass.toString().removePrefix("class ")}

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val currentBackStackEntry = navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStackEntry.value?.destination?.route
            val isBaseRoute = baseRoutes.contains(currentRoute)

            TopAppBar(
                modifier = Modifier
                    .height(90.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = currentScreenTitle,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    if (!isBaseRoute) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 8.dp)
                        ) {
                            IconButton(
                                onClick = { navController.popBackStack() }
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        IconButton(
                            onClick = { /* Open notifications */ }
                        ) {
                            Icon(
                                painterResource(id = R.drawable.notif),
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val currentBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry.value?.destination?.route

                navItemList.forEach { navItem ->
                    NavigationBarItem(
                        selected = currentRoute == navItem.route.javaClass.toString().removePrefix("class "),
                        icon = { Icon(imageVector = navItem.icon, contentDescription = navItem.label) },
                        label = { Text(navItem.label) },
                        onClick = {
                            navController.navigate(navItem.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            //homeDestination(goToPortfolioViewer = {session_name -> navController.navigate(Portfolio(session_name))})
            homeDestination(goToLeagueScreen = {leagueJson -> navController.navigate(LeagueScreen(leagueJson))})
            newsDestination(navController)
            profileDestination(
                onNavigateToFriendsList = {
                    navController.navigate(FriendsList)
                },
                onNavigateToChangePassword = onNavigateToChangePassword,
                onSignOut = onSignOut
            )
            stocksDestination(goToStockViewer = {stock -> navController.navigate(Stock(stock))})
            stockViewer()
            portfolioViewer(goToStockViewer = {stock -> navController.navigate(Stock(stock))})
            leagueScreenViewer(goToStockViewer = {stock -> navController.navigate(Stock(stock))})
            // TODO: For some reason the nav bar gets greyed out when navigating to news article
//            composable(
//                route = "news_article?articlePrimaryKey={article.primaryKey}",
//                arguments = listOf(navArgument("articlePrimaryKey") { defaultValue = -1 })
//            ) { backStackEntry ->
//                val articlePrimaryKey = backStackEntry.arguments?.getString("articlePrimaryKey")?.toIntOrNull() ?: -1
//                NewsArticle(navController, articlePrimaryKey)
//            }
            composable<Profile> { backStackEntry ->
                val name = backStackEntry.arguments?.getString("name") ?: "Unknown"
                ProfileScreen(
                    onNavigateToFriendsList = {
                        navController.navigate(FriendsList)
                    },
                    onNavigateToChangePassword = onNavigateToChangePassword,
                    onSignOut = onSignOut
                )
            }

            composable<FriendsList> {
                FriendsListScreen()
            }
        }
    }
}
