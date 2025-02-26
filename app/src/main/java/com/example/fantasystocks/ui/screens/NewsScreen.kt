package com.example.fantasystocks.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.serialization.Serializable
import com.example.fantasystocks.R

@Serializable
object News

data class NewsArticle(val primaryKey: Int, val title: String, val description: String, val imageRes: Int)

val newsArticles = listOf(
    NewsArticle(1, "Investment Strategies 101", "Beginner-friendly guide to investing", R.drawable.ic_launcher_background),
    NewsArticle(2, "Virtual Stock Market", "Compete with friends in simulated trading", R.drawable.ic_launcher_foreground),
    NewsArticle(3, "Market Trends 2024", "Latest updates on stock market trends", R.drawable.ic_launcher_background),
    NewsArticle(4, "Crypto Insights", "Deep dive into the cryptocurrency world", R.drawable.ic_launcher_foreground),
    NewsArticle(5, "Tech Stocks to Watch", "Emerging tech stocks with potential", R.drawable.ic_launcher_background),
    NewsArticle(6, "Real Estate Investing", "Understanding the housing market and investment opportunities", R.drawable.ic_launcher_foreground)
)

fun NavGraphBuilder.newsDestination(navController: NavController) {
    composable<News> { NewsScreen(navController) }
    composable(
        "news_article?articlePrimaryKey={articlePrimaryKey}",
        arguments = listOf(navArgument("articlePrimaryKey") { defaultValue = -1 })
    ) { backStackEntry ->
        val articlePrimaryKey = backStackEntry.arguments?.getInt("articlePrimaryKey") ?: -1
        com.example.fantasystocks.ui.news.NewsArticle(navController, articlePrimaryKey)
    }
}

@Composable
fun NewsScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Recommended for You",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(newsArticles.size) { index ->
            val article = newsArticles[index]
            NewsCard(
                title = article.title,
                description = article.description,
                imageRes = article.imageRes,
                onClick = {
                    navController.navigate("news_article?articlePrimaryKey=${article.primaryKey}") {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun NewsCard(
    title: String,
    description: String,
    imageRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Column {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun NewsArticleScreen(navController: NavController, articlePrimaryKey: Int) {
    val article = newsArticles.find { it.primaryKey == articlePrimaryKey }

    Column(modifier = Modifier.padding(16.dp)) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        if (article != null) {
            Text(text = article.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Full article content for ${article.title}", style = MaterialTheme.typography.bodyMedium)
        } else {
            Text(text = "Article not found", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
