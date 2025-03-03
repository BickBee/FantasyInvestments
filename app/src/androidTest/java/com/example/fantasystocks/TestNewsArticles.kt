package com.example.fantasystocks

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.fantasystocks.ui.NewsScreen
import com.example.fantasystocks.ui.news.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestNewsArticles {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var testArticle: NewsArticleContent

    @Before
    fun setup() {
        testArticle = NewsArticleContent(
            primaryKey = 1,
            title = "Sample News",
            description = "This is a sample news article.",
            imageRes = 0, // Mocked resource ID
            content = """
                <article>
                    <text>Sample news content.</text>
                    <quiz question="What is 2 + 2?" correctAnswer="4" />
                </article>
            """.trimIndent()
        )
    }

    @Test
    fun testArticleTitleDisplaysCorrectly() {
        composeTestRule.setContent {
            ArticleTitle("Breaking News")
        }
        composeTestRule.onNodeWithText("Breaking News").assertIsDisplayed()
    }

    @Test
    fun testTrueFalseQuestionHandlesCorrectAnswer() {
        composeTestRule.setContent {
            TrueFalseQuestion(question = "Is Kotlin a language?", correctAnswer = true)
        }
        composeTestRule.onNodeWithText("True").performClick()
        composeTestRule.onNodeWithText("Correct! 🎉").assertIsDisplayed()
    }

    @Test
    fun testTrueFalseQuestionHandlesIncorrectAnswer() {
        composeTestRule.setContent {
            TrueFalseQuestion(question = "Is JavaScript the same as Java?", correctAnswer = false)
        }
        composeTestRule.onNodeWithText("True").performClick()
        composeTestRule.onNodeWithText("Incorrect! ❌").assertIsDisplayed()
    }

    @Test
    fun testQuizQuestionHandlesCorrectAnswer() {
        composeTestRule.setContent {
            QuizQuestion(question = "What is 2 + 2?", correctAnswer = "4")
        }

        val inputField = composeTestRule.onNode(hasText("Your Answer") and hasSetTextAction())
        inputField.performClick()
        inputField.performTextInput("4")
        composeTestRule.onNodeWithText("Submit").performClick()
        composeTestRule.onNodeWithText("Correct! 🎉").assertIsDisplayed()
    }

    @Test
    fun testNewsScreenDisplaysArticles() {
        composeTestRule.setContent {
            NewsScreen(navController = TestNavHostController(ApplicationProvider.getApplicationContext()))
        }
        composeTestRule.onNodeWithText("Recommended for You").assertIsDisplayed()
    }

    @Test
    fun testBackButtonNavigatesBack() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        composeTestRule.setContent {
            BackButton(navController)
        }
        composeTestRule.onNodeWithText("Return To News Page").performClick()
    }
}
