package com.example.fantasystocks.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.fantasystocks.ui.news.newsArticleContents

@Serializable
object News

fun NavGraphBuilder.newsDestination(navController: NavController) {
    composable<News> { NewsScreen(navController) }
    composable(
        "news_article?articlePrimaryKey={articlePrimaryKey}",
        arguments = listOf(navArgument("articlePrimaryKey") { defaultValue = -1 })
    ) { backStackEntry ->
        val articlePrimaryKey = backStackEntry.arguments?.getInt("articlePrimaryKey") ?: -1
        com.example.fantasystocks.ui.news.NewsArticle(navController, newsArticleContents[articlePrimaryKey - 1])
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

        items(newsArticleContents.size) { index ->
            val article = newsArticleContents[index]
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