package com.example.fantasystocks.ui.news

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fantasystocks.R
import kotlinx.serialization.Serializable
import androidx.compose.ui.text.style.TextAlign

@Serializable
object NewsArticle

fun NavGraphBuilder.newsArticleDestination(navController: NavController, articlePrimaryKey: Int) {
    composable<NewsArticle> { NewsArticle(navController, articlePrimaryKey) }
}

@Composable
fun NewsArticle(navController: NavController, articlePrimaryKey: Int) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ------------------------------- ARTICLE CONTENT -------------------------------
        item {
            ArticleTitle(title = "${articlePrimaryKey}: Breaking News: Market Hits Record Highs!")
        }


        item {
            ArticleImage(
                imageResId = R.drawable.ic_launcher_background, // Replace with actual image resource
                contentDescription = "Article Image"
            )
        }

        item {
            TrueFalseQuestion(
                question = "The Eiffel Tower is in London.",
                correctAnswer = false
            )
        }

        item {
            ArticleText(
                content = "Lorem ipsum odor amet, consectetuer adipiscing elit. Justo dictumst ligula pellentesque sit ad volutpat. Imperdiet vel neque fermentum massa nisl bibendum ultricies. Purus semper aliquam diam, quam ipsum etiam nullam mattis. Viverra orci eros vulputate vivamus; dapibus nostra convallis tincidunt? Penatibus etiam erat fringilla integer parturient egestas. Lacinia lacus erat quis condimentum pellentesque euismod ex netus. Amet augue curae; purus ipsum vel laoreet. Mollis vivamus efficitur velit inceptos sed, etiam accumsan inceptos duis. Amet fames viverra ultricies feugiat placerat quam odio pulvinar.\n" +
                        "\n" +
                        "Dictumst per mus pretium imperdiet orci; bibendum penatibus penatibus arcu. Facilisi potenti amet erat in parturient. Suscipit morbi feugiat mauris ligula vitae eget; sapien hac pellentesque. Maecenas nibh cubilia sem, diam malesuada sed. Pellentesque posuere gravida leo vulputate sollicitudin ex. Fusce sit blandit elit cursus malesuada conubia. Blandit lectus inceptos facilisi blandit neque luctus pulvinar fames. Imperdiet fermentum libero vestibulum phasellus nostra proin. Aptent est turpis suspendisse eros; nullam pulvinar habitant.\n" +
                        "\n" +
                        "Ac massa sodales laoreet urna condimentum montes blandit felis. Maecenas pretium libero quam et velit dictum. Eget aliquet mus ipsum euismod class tristique efficitur. Senectus ornare pellentesque habitasse semper habitasse tristique nibh conubia. Ex pellentesque sagittis sagittis dignissim diam cras. Dui neque pulvinar scelerisque purus vel rutrum erat. Fermentum id elementum consectetur fringilla phasellus tincidunt nunc proin. Curabitur lorem ultrices cubilia tortor viverra odio blandit adipiscing neque? Molestie porttitor porttitor tellus quam posuere ligula.\n" +
                        "\n" +
                        "Cras efficitur parturient nisl volutpat varius magna a. Class cursus dapibus arcu malesuada etiam phasellus sociosqu ullamcorper. Justo litora euismod, nam proin urna bibendum. Nullam sodales consectetur magnis amet efficitur sagittis. Efficitur natoque per purus in inceptos suspendisse. Lobortis fusce mollis imperdiet aliquet pulvinar phasellus. Pulvinar nec hac duis ornare magna turpis lorem. Fusce mi consequat taciti magna suscipit duis leo. Viverra enim auctor suscipit porttitor scelerisque habitant ultricies netus. Fames mollis montes, pulvinar suspendisse nam platea tortor habitasse.\n" +
                        "\n" +
                        "Condimentum faucibus etiam urna venenatis magnis nam egestas condimentum ante. Praesent sapien taciti taciti, ad justo class fusce vehicula. Mus aptent nibh dictum dis blandit. Vitae varius maximus; pellentesque tellus augue vestibulum. Mollis mauris sollicitudin malesuada faucibus; donec risus velit dui. Congue varius ligula felis felis efficitur rutrum egestas. Semper ante vulputate gravida taciti nisl sit fusce praesent."
            )
        }

        item {
            QuizQuestion(
                question = "What is the capital of France?",
                correctAnswer = "Paris"
            )
        }

        item {
            MultipleChoiceQuestion(
                question = "Which planet is known as the Red Planet?",
                options = listOf("Earth", "Mars", "Venus", "Jupiter"),
                correctAnswer = "Mars"
            )
        }

        item {
            BackButton(navController)
        }
    }
}

@Composable
fun ArticleTitle(title: String) {
    Card(
        shape = RoundedCornerShape(16.dp), // Rounded corners for aesthetics
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Adds a subtle shadow
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = title, // Uses dynamic title text
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center, // Centers the text
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Adds spacing inside the card
        )
    }
}

@Composable
fun ArticleText(content: String) {
    Card(
        shape = RoundedCornerShape(16.dp), // Rounded corners for aesthetics
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Subtle shadow effect
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(16.dp) // Adds padding inside the Card
        )
    }
}

@Composable
fun ArticleImage(imageResId: Int, contentDescription: String) {
    Card(
        shape = RoundedCornerShape(16.dp), // Rounded corners
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Adds subtle shadow
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 8.dp) // Adds spacing
    ) {
        Image(
            painter = painterResource(id = imageResId), // Dynamic image resource
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun TrueFalseQuestion(question: String, correctAnswer: Boolean) {
    var userAnswer by remember { mutableStateOf<Boolean?>(null) }
    var feedback by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "True or False?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        userAnswer = true
                        feedback = if (correctAnswer) "Correct! 🎉" else "Incorrect! ❌"
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (userAnswer == true) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.primary)
                ) {
                    Text("True")
                }

                Button(
                    onClick = {
                        userAnswer = false
                        feedback = if (!correctAnswer) "Correct! 🎉" else "Incorrect! ❌"
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (userAnswer == false) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.primary)
                ) {
                    Text("False")
                }
            }

            if (feedback.isNotEmpty()) {
                Text(
                    text = feedback,
                    color = if (feedback.contains("Correct")) Color.Green else Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun QuizQuestion(question: String, correctAnswer: String) {
    var userAnswer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Quiz Question",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = userAnswer,
                onValueChange = { userAnswer = it },
                label = { Text("Your Answer") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    feedback = if (userAnswer.trim().equals(correctAnswer, ignoreCase = true)) {
                        "Correct! 🎉"
                    } else {
                        "Incorrect! Try again. ❌"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Submit")
            }

            if (feedback.isNotEmpty()) {
                Text(
                    text = feedback,
                    color = if (feedback.contains("Correct")) Color.Green else Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MultipleChoiceQuestion(question: String, options: List<String>, correctAnswer: String) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var feedback by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            options.forEach { option ->
                Button(
                    onClick = {
                        selectedOption = option
                        feedback = if (option == correctAnswer) "Correct! 🎉" else "Incorrect! ❌"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedOption == option) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(option)
                }
            }

            if (feedback.isNotEmpty()) {
                Text(
                    text = feedback,
                    color = if (feedback.contains("Correct")) Color.Green else Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun BackButton(navController: NavController) {
    Card(
        shape = RoundedCornerShape(16.dp), // Matches quiz styling
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Button(
            onClick = { navController.popBackStack() }, // Navigate back
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Return To News Page")
        }
    }
}