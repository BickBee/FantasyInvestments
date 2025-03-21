//package com.example.fantasystocks.ui.viewmodels
//
//import android.util.Log
//import com.example.fantasystocks.database.SupabaseClient
//import io.github.jan.supabase.auth.user.UserInfo
//import io.github.jan.supabase.exceptions.RestException
//import io.mockk.Runs
//import io.mockk.coEvery
//import io.mockk.coVerify
//import io.mockk.every
//import io.mockk.just
//import io.mockk.mockk
//import io.mockk.mockkObject
//import io.mockk.mockkStatic
//import io.mockk.unmockkAll
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.StandardTestDispatcher
//import kotlinx.coroutines.test.advanceUntilIdle
//import kotlinx.coroutines.test.resetMain
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.test.setMain
//import org.junit.After
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertFalse
//import org.junit.Assert.assertTrue
//import org.junit.Before
//import org.junit.Test
//
//@ExperimentalCoroutinesApi
//class AuthViewModelTest {
//
//    private lateinit var authViewModel: AuthViewModel
//    private val testDispatcher = StandardTestDispatcher()
//
//    @Before
//    fun setUp() {
//        Dispatchers.setMain(testDispatcher)
//        mockkObject(SupabaseClient)
//        mockkStatic(Log::class)
//
//        // Mock all Log calls
//        every { Log.d(any(), any()) } returns 0
//        every { Log.e(any(), any()) } returns 0
//        every { Log.e(any(), any(), any()) } returns 0
//        every { Log.w(any<String>(), any<String>()) } returns 0
//        every { Log.w(any<String>(), any<Throwable>()) } returns 0
//
//        // Mock SupabaseClient methods
//        every { SupabaseClient.isAuthenticated() } returns false
//        every { SupabaseClient.getCurrentUser() } returns null
//
//        authViewModel = AuthViewModel()
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//        unmockkAll()
//    }
//
//    @Test
//    fun `checkAuthState updates isAuthenticated when user is logged in`() = runTest {
//        // Arrange
//        val mockUser = mockk<UserInfo>()
//        every { mockUser.email } returns "test@example.com"
//        every { SupabaseClient.getCurrentUser() } returns mockUser
//
//        // Act
//        authViewModel.checkAuthState()
//        advanceUntilIdle()
//
//        // Assert
//        assertTrue(authViewModel.isAuthenticated.value == true)
//    }
//
//    @Test
//    fun `checkAuthState updates isAuthenticated to false when no user is logged in`() = runTest {
//        // Arrange
//        every { SupabaseClient.isAuthenticated() } returns false
//        every { SupabaseClient.getCurrentUser() } returns null
//
//        // Act
//        authViewModel.checkAuthState()
//        advanceUntilIdle()
//
//        // Assert
//        assertFalse(authViewModel.isAuthenticated.value == true)
//    }
//
//    @Test
//    fun `signInWithEmail sets isAuthenticated to true on successful login`() = runTest {
//        // Arrange
//        coEvery { SupabaseClient.signInExistingUser(any(), any()) } just Runs
//        every { SupabaseClient.isAuthenticated() } returns true
//        val mockUser = mockk<UserInfo>()
//        every { mockUser.email } returns "test@example.com"
//        every { SupabaseClient.getCurrentUser() } returns mockUser
//
//        // Act
//        authViewModel.signInWithEmail("test@example.com", "password")
//        advanceUntilIdle()
//
//        // Assert
//        assertTrue(authViewModel.isAuthenticated.value == true)
//        assertEquals(null, authViewModel.errorMessage.value)
//        assertFalse(authViewModel.isLoading.value)
//    }
//
//    @Test
//    fun `signInWithEmail sets error message on failed login`() = runTest {
//        // Arrange
//        val exception = mockk<RestException>()
//        every { exception.message } returns "Invalid credentials"
//        coEvery { SupabaseClient.signInExistingUser(any(), any()) } throws exception
//
//        // Act
//        authViewModel.signInWithEmail("test@example.com", "wrong-password")
//        advanceUntilIdle()
//
//        // Assert
//        assertFalse(authViewModel.isAuthenticated.value == true)
//        assertEquals("Invalid credentials", authViewModel.errorMessage.value)
//        assertFalse(authViewModel.isLoading.value)
//    }
//
//    @Test
//    fun `signUpWithEmail validates password length`() = runTest {
//        // Arrange - No need to mock sign up call as it should not be reached
//
//        // Act
//        authViewModel.signUpWithEmail("test@example.com", "short")
//        advanceUntilIdle()
//
//        // Assert
//        assertEquals("Password must be at least 6 characters long", authViewModel.errorMessage.value)
//        assertFalse(authViewModel.isLoading.value)
//        // Verify that signUpNewUser was not called
//        coVerify(exactly = 0) { SupabaseClient.signUpNewUser(any(), any()) }
//    }
//
//    @Test
//    fun `signUpWithEmail sets isAuthenticated to true on successful signup`() = runTest {
//        // Arrange
//        coEvery { SupabaseClient.signUpNewUser(any(), any()) } just Runs
//        every { SupabaseClient.isAuthenticated() } returns true
//        val mockUser = mockk<UserInfo>()
//        every { mockUser.email } returns "newuser@example.com"
//        every { SupabaseClient.getCurrentUser() } returns mockUser
//
//        // Act
//        authViewModel.signUpWithEmail("newuser@example.com", "password123")
//        advanceUntilIdle()
//
//        // Assert
//        assertTrue(authViewModel.isAuthenticated.value == true)
//        assertEquals(null, authViewModel.errorMessage.value)
//        assertFalse(authViewModel.isLoading.value)
//        coVerify { SupabaseClient.signUpNewUser("newuser@example.com", "password123") }
//    }
//
//    @Test
//    fun `signOut sets isAuthenticated to false`() = runTest {
//        // Arrange
//        coEvery { SupabaseClient.signOut() } just Runs
//
//        // Act
//        authViewModel.signOut()
//        advanceUntilIdle()
//
//        // Assert
//        assertFalse(authViewModel.isAuthenticated.value == true)
//        coVerify { SupabaseClient.signOut() }
//    }
//
//    @Test
//    fun `clearErrors sets errorMessage to null`() {
//        // Arrange
//        val viewModel = AuthViewModel()
//        viewModel.clearErrors()
//
//        // Assert
//        assertEquals(null, viewModel.errorMessage.value)
//    }
//}