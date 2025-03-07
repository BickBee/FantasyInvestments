package com.example.fantasystocks.database

import android.content.Context
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.mockk.slot

@RunWith(RobolectricTestRunner::class)
class SupabaseClientTest {
    
    private lateinit var mockContext: Context
    private lateinit var mockSupabaseClient: SupabaseClient
    private lateinit var mockAuth: Auth
    
    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        mockSupabaseClient = mockk(relaxed = true)
        mockAuth = mockk(relaxed = true)
        
        mockkObject(com.example.fantasystocks.database.SupabaseClient)
        mockkStatic(::createSupabaseClient)
        
        every { createSupabaseClient(any(), any(), any()) } returns mockSupabaseClient
        every { mockSupabaseClient.auth } returns mockAuth
        every { mockContext.filesDir.absolutePath } returns "/test/path"
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `test initialization sets up Supabase client correctly`() {
        // Assert
        assertEquals(mockSupabaseClient, com.example.fantasystocks.database.SupabaseClient.supabase)
    }
    
    @Test
    fun `test isAuthenticated returns true when session exists`() {
        // Arrange
        every { mockAuth.currentSessionOrNull() } returns mockk()
        
        // Act
        val result = com.example.fantasystocks.database.SupabaseClient.isAuthenticated()
        
        // Assert
        assertTrue(result)
    }
    
    @Test
    fun `test isAuthenticated returns false when no session exists`() {
        // Arrange
        every { mockAuth.currentSessionOrNull() } returns null
        
        // Act
        val result = com.example.fantasystocks.database.SupabaseClient.isAuthenticated()
        
        // Assert
        assertFalse(result)
    }
    
    @Test
    fun `test signInExistingUser calls Supabase auth correctly`() = runBlocking {
        // Arrange
        val testEmail = "test@example.com"
        val testPassword = "password123"
        
        coEvery { mockAuth.signInWith(Email) } returns mockk()
        
        // Act
        com.example.fantasystocks.database.SupabaseClient.signInExistingUser(testEmail, testPassword)
        
        // Assert
        coVerify { mockAuth.signInWith(Email) }
    }
    
    @Test
    fun `test signUpNewUser calls Supabase auth correctly`() = runBlocking {
        // Arrange
        val testEmail = "newuser@example.com"
        val testPassword = "newpassword123"

        coEvery { mockAuth.signUpWith(Email) } returns mockk()
        
        // Act
        com.example.fantasystocks.database.SupabaseClient.signUpNewUser(testEmail, testPassword)
        
        // Assert
        coVerify { mockAuth.signUpWith(Email) }
    }
    
    @Test
    fun `test signOut calls Supabase auth signOut`() = runBlocking {
        // Arrange
        coEvery { mockAuth.signOut() } returns Unit
        
        // Act
        com.example.fantasystocks.database.SupabaseClient.signOut()
        
        // Assert
        coVerify { mockAuth.signOut() }
    }
}