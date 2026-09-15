package com.example.dsarecall.data.repository

import com.example.dsarecall.domain.model.AuthState
import com.example.dsarecall.domain.model.AuthUser
import com.example.dsarecall.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class MockAuthRepositoryImpl : AuthRepository {
    private val registeredUsers = mutableMapOf<String, String>()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override val currentUser: AuthUser?
        get() = when (val state = _authState.value) {
            is AuthState.Authenticated -> state.user
            else -> null
        }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || password.length < 6) {
            val error = "Password must be at least 6 characters"
            _authState.value = AuthState.Error(error)
            return Result.failure(IllegalArgumentException(error))
        }

        if (!registeredUsers.containsKey(cleanEmail)) {
            val error = "No account found with this email. Please tap 'Create Account' first."
            _authState.value = AuthState.Error(error)
            return Result.failure(IllegalArgumentException(error))
        }

        val savedPassword = registeredUsers[cleanEmail]
        if (savedPassword != password) {
            val error = "Incorrect password. Please try again."
            _authState.value = AuthState.Error(error)
            return Result.failure(IllegalArgumentException(error))
        }

        val user = AuthUser(
            id = "user_" + UUID.nameUUIDFromBytes(cleanEmail.toByteArray()),
            email = email,
            displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
            isAnonymous = false
        )
        _authState.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<AuthUser> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || password.length < 6) {
            val error = "Password must be at least 6 characters"
            _authState.value = AuthState.Error(error)
            return Result.failure(IllegalArgumentException(error))
        }

        if (registeredUsers.containsKey(cleanEmail)) {
            val error = "An account with this email already exists. Please tap 'Sign In' instead."
            _authState.value = AuthState.Error(error)
            return Result.failure(IllegalArgumentException(error))
        }

        registeredUsers[cleanEmail] = password
        val user = AuthUser(
            id = "user_" + UUID.nameUUIDFromBytes(cleanEmail.toByteArray()),
            email = email,
            displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
            isAnonymous = false
        )
        _authState.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun signInAnonymously(): Result<AuthUser> {
        val user = AuthUser(
            id = "guest_" + UUID.randomUUID().toString().take(8),
            displayName = "Guest Candidate",
            isAnonymous = true
        )
        _authState.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun signInWithGoogleToken(idToken: String): Result<AuthUser> {
        val user = AuthUser(
            id = "google_user_123",
            email = "candidate@gmail.com",
            displayName = "Google Candidate",
            isAnonymous = false
        )
        _authState.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun signOut() {
        _authState.value = AuthState.Unauthenticated
    }
}
