package com.example.fantasystocks.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fantasystocks.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.delay

sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("login")
    object Signup : AuthScreen("signup")
    object ForgotPassword : AuthScreen("forgot_password")
    object ChangePassword : AuthScreen("change_password")
}

fun NavGraphBuilder.authScreens(
    navController: NavController,
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    composable(AuthScreen.Login.route) {
        LoginScreen(
            authViewModel = authViewModel,
            onNavigateToSignup = { navController.navigate(AuthScreen.Signup.route) },
            onNavigateToForgotPassword = { navController.navigate(AuthScreen.ForgotPassword.route) },
            onLoginSuccess = onLoginSuccess
        )
    }
    
    composable(AuthScreen.Signup.route) {
        SignupScreen(
            authViewModel = authViewModel,
            onNavigateToLogin = { navController.popBackStack() },
            onSignupSuccess = onLoginSuccess
        )
    }
    
    composable(AuthScreen.ForgotPassword.route) {
        ForgotPasswordScreen(
            authViewModel = authViewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
    
    composable(AuthScreen.ChangePassword.route) {
        ChangePasswordScreen(
            authViewModel = authViewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    // Clear any previous errors when entering the screen
    LaunchedEffect(Unit) {
        authViewModel.clearErrors()
    }

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated == true) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Fantasy Investments",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = !isLoading
        )

        TextButton(
            onClick = onNavigateToForgotPassword,
            modifier = Modifier
                .align(Alignment.End)
                .padding(vertical = 8.dp),
            enabled = !isLoading
        ) {
            Text("Forgot Password?")
        }

        Button(
            onClick = {
                authViewModel.signInWithEmail(email, password)
            },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Sign In")
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Don't have an account?")
            TextButton(
                onClick = onNavigateToSignup,
                enabled = !isLoading
            ) {
                Text("Sign Up")
            }
        }
    }
}

@Composable
fun SignupScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val isUsernameCheckLoading by authViewModel.isUsernameCheckLoading.collectAsState()
    val isUsernameAvailable by authViewModel.isUsernameAvailable.collectAsState()

    // Clear any previous errors when entering the screen
    LaunchedEffect(Unit) {
        authViewModel.clearErrors()
    }

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated == true) {
            onSignupSuccess()
        }
    }

    // When username changes, check for errors with debounce
    LaunchedEffect(username) {
        if (username.isNotEmpty()) {
            if (username.length < 3) {
                usernameError = "Username must be at least 3 characters long"
            } else if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                usernameError = "Username can only contain letters, numbers, and underscores"
            } else {
                usernameError = null
                // Add delay before checking availability
                delay(500) // Wait 500ms after last keystroke
                if (username.length >= 3 && username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                    authViewModel.checkUsernameAvailability(username)
                }
            }
        } else {
            usernameError = null
        }
    }

    // When we get a response about username availability
    LaunchedEffect(isUsernameAvailable) {
        if (isUsernameAvailable == false && username.isNotEmpty() && usernameError == null) {
            usernameError = "Username already taken"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = !isLoading,
            isError = usernameError != null,
            trailingIcon = {
                if (isUsernameCheckLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else if (username.isNotEmpty() && username.length >= 3 && username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                    if (isUsernameAvailable == true) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Username available",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else if (isUsernameAvailable == false) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Username taken",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            supportingText = {
                if (usernameError != null) {
                    Text(usernameError!!, color = MaterialTheme.colorScheme.error)
                } else if (username.isNotEmpty() && username.length >= 3 && username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                    if (isUsernameAvailable == true) {
                        Text("Username is available", color = MaterialTheme.colorScheme.primary)
                    } else if (isUsernameAvailable == false) {
                        Text("Username is already taken", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                if (confirmPassword.isNotEmpty()) {
                    passwordError = if (it != confirmPassword) "Passwords do not match" else null
                }
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            isError = passwordError != null,
            enabled = !isLoading
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                passwordError = if (it != password) "Passwords do not match" else null
            },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            isError = passwordError != null,
            enabled = !isLoading,
            supportingText = {
                if (passwordError != null) {
                    Text(passwordError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Button(
            onClick = {
                if (password == confirmPassword && usernameError == null) {
                    authViewModel.signUpWithEmail(email, password, username)
                } else {
                    passwordError = "Passwords do not match"
                }
            },
            enabled = !isLoading && 
                     email.isNotBlank() && 
                     username.isNotBlank() && 
                     password.isNotBlank() && 
                     confirmPassword.isNotBlank() && 
                     passwordError == null && 
                     usernameError == null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Sign Up")
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Already have an account?")
            TextButton(
                onClick = onNavigateToLogin,
                enabled = !isLoading
            ) {
                Text("Sign In")
            }
        }

        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    val isPasswordResetSent by authViewModel.isPasswordResetSent.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    
    // Clear any previous state when entering the screen
    LaunchedEffect(Unit) {
        authViewModel.clearErrors()
        authViewModel.resetState()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (isPasswordResetSent) {
            Text(
                text = "Reset link sent to your email. Please check your inbox.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(50.dp)
            ) {
                Text("Back to Login")
            }
        } else {
            Text(
                text = "Enter your email address and we'll send you a link to reset your password.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                enabled = !isLoading
            )

            Button(
                onClick = {
                    authViewModel.resetPassword(email)
                },
                enabled = !isLoading && email.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Send Reset Link")
                }
            }

            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                enabled = !isLoading
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
fun ChangePasswordScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    val isPasswordChanged by authViewModel.isPasswordChanged.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Clear any previous state when entering the screen
    LaunchedEffect(Unit) {
        authViewModel.clearErrors()
        authViewModel.resetState()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Change Password",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (isPasswordChanged) {
            Text(
                text = "Password changed successfully!",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(50.dp)
            ) {
                Text("Back to Profile")
            }
        } else {
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                enabled = !isLoading
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { 
                    newPassword = it 
                    if (confirmNewPassword.isNotEmpty()) {
                        passwordError = if (it != confirmNewPassword) "Passwords do not match" else null
                    }
                },
                label = { Text("New Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                isError = passwordError != null,
                enabled = !isLoading
            )

            OutlinedTextField(
                value = confirmNewPassword,
                onValueChange = { 
                    confirmNewPassword = it
                    passwordError = if (it != newPassword) "Passwords do not match" else null
                },
                label = { Text("Confirm New Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                isError = passwordError != null,
                enabled = !isLoading
            )

            passwordError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                )
            }

            Button(
                onClick = {
                    if (newPassword == confirmNewPassword) {
                        authViewModel.changePassword(currentPassword, newPassword)
                    } else {
                        passwordError = "Passwords do not match"
                    }
                },
                enabled = !isLoading && 
                         currentPassword.isNotBlank() && 
                         newPassword.isNotBlank() && 
                         confirmNewPassword.isNotBlank() && 
                         passwordError == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Change Password")
                }
            }

            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    }
}