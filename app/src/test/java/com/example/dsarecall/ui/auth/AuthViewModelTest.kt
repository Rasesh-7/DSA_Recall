package com.example.dsarecall.ui.auth

import com.example.dsarecall.data.repository.MockAuthRepositoryImpl
import com.example.dsarecall.domain.model.AuthState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private lateinit var mockAuthRepository: MockAuthRepositoryImpl

    @Before
    fun setUp() {
        mockAuthRepository = MockAuthRepositoryImpl()
    }

    @Test
    fun `initial auth state is unauthenticated`() {
        assertTrue(mockAuthRepository.authState.value is AuthState.Unauthenticated)
    }

    @Test
    fun `signInAnonymously sets state to Authenticated guest`() = runBlocking {
        val result = mockAuthRepository.signInAnonymously()

        assertTrue(result.isSuccess)
        val state = mockAuthRepository.authState.value
        assertTrue(state is AuthState.Authenticated)
        assertEquals(true, (state as AuthState.Authenticated).user.isAnonymous)
    }

    @Test
    fun `signInWithEmail with valid credentials authenticates user`() = runBlocking {
        mockAuthRepository.signUpWithEmail("test@example.com", "secure123")
        val result = mockAuthRepository.signInWithEmail("test@example.com", "secure123")

        assertTrue(result.isSuccess)
        val state = mockAuthRepository.authState.value
        assertTrue(state is AuthState.Authenticated)
        assertEquals("test@example.com", (state as AuthState.Authenticated).user.email)
    }

    @Test
    fun `signOut resets state to Unauthenticated`() = runBlocking {
        mockAuthRepository.signInAnonymously()
        mockAuthRepository.signOut()

        assertTrue(mockAuthRepository.authState.value is AuthState.Unauthenticated)
    }
}
