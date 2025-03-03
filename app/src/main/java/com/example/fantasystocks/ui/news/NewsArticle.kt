package com.example.fantasystocks.ui.news

// Import custom composables
import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fantasystocks.R
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

fun NavGraphBuilder.newsArticleDestination(navController: NavController, article: NewsArticleContent) {
    composable("news_article/${article.primaryKey}") {
        NewsArticle(navController, article)
    }
}

@Composable
fun NewsArticle(navController: NavController, article: NewsArticleContent) {
    val doc: Document = Jsoup.parse(article.content)
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item { ArticleTitle(article.title) }
        doc.select("article").first()?.children()?.forEach { element ->
            when (element.tagName()) {
                "articleimage" -> {
                    val imageResId = getDrawableResource(context, element.text()) ?: R.drawable.image_not_found
                    item { ArticleImage(imageResId = imageResId, contentDescription = "Article Image") }
                }
                "text" -> item { ArticleText(content = element.text()) }
                "truefalse" -> {
                    val question = element.attr("question")
                    val correctAnswer = element.attr("correctAnswer").toBoolean()
                    item { TrueFalseQuestion(question = question, correctAnswer = correctAnswer) }
                }
                "quiz" -> {
                    val question = element.attr("question")
                    val correctAnswer = element.attr("correctAnswer")
                    item { QuizQuestion(question = question, correctAnswer = correctAnswer) }
                }
                "multiplechoice" -> {
                    val question = element.attr("question")
                    val correctAnswer = element.attr("correctAnswer")
                    val options = element.select("option").map(Element::text)
                    item { MultipleChoiceQuestion(question = question, options = options, correctAnswer = correctAnswer) }
                }
            }
        }
        item { BackButton(navController) }
    }
}

/**
 * Utility function to convert image name from XML to actual resource ID.
 */
fun getDrawableResource(context: Context, imageName: String): Int? {
    return context.resources.getIdentifier(imageName, "drawable", context.packageName)
        .takeIf { it != 0 }
}
