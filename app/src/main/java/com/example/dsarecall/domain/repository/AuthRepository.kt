package com.example.dsarecall.domain.repository

import com.example.dsarecall.domain.model.AuthState
import com.example.dsarecall.domain.model.AuthUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>
    val currentUser: AuthUser?
    
    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun signUpWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun signInAnonymously(): Result<AuthUser>
    suspend fun signInWithGoogleToken(idToken: String): Result<AuthUser>
    suspend fun signOut()
}
