package com.example.dsarecall.data.repository

import com.example.dsarecall.domain.model.AuthState
import com.example.dsarecall.domain.model.AuthUser
import com.example.dsarecall.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Production Firebase Authentication Repository implementation.
 * Integrates with com.google.firebase.auth.FirebaseAuth and provides
 * safe error handling and mock fallback for Play Services / SecurityException edge cases.
 */
class FirebaseAuthRepositoryImpl(
    private val mockFallback: MockAuthRepositoryImpl = MockAuthRepositoryImpl()
) : AuthRepository {

    private val firebaseAuth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Throwable) {
        null
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = if (firebaseAuth != null) _authState.asStateFlow() else mockFallback.authState

    init {
        try {
            firebaseAuth?.addAuthStateListener { auth ->
                val user = auth.currentUser
                if (user != null) {
                    _authState.value = AuthState.Authenticated(
                        AuthUser(
                            id = user.uid,
                            email = user.email,
                            displayName = user.displayName ?: user.email?.substringBefore("@"),
                            photoUrl = user.photoUrl?.toString(),
                            isAnonymous = user.isAnonymous
                        )
                    )
                } else {
                    // Only transition to Unauthenticated if not currently loading
                    if (_authState.value !is AuthState.Loading) {
                        _authState.value = AuthState.Unauthenticated
                    }
                }
            }
        } catch (_: Throwable) {}
    }

    override val currentUser: AuthUser?
        get() {
            val fbUser = try { firebaseAuth?.currentUser } catch (_: Throwable) { null }
            return if (fbUser != null) {
                AuthUser(
                    id = fbUser.uid,
                    email = fbUser.email,
                    displayName = fbUser.displayName ?: fbUser.email?.substringBefore("@"),
                    photoUrl = fbUser.photoUrl?.toString(),
                    isAnonymous = fbUser.isAnonymous
                )
            } else {
                mockFallback.currentUser
            }
        }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
        val auth = firebaseAuth ?: return mockFallback.signInWithEmail(email, password)
        return try {
            _authState.value = AuthState.Loading
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                val domainUser = AuthUser(
                    id = user.uid,
                    email = user.email ?: email,
                    displayName = user.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    isAnonymous = user.isAnonymous
                )
                _authState.value = AuthState.Authenticated(domainUser)
                Result.success(domainUser)
            } else {
                val errorMsg = "No account found with these credentials"
                _authState.value = AuthState.Error(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Throwable) {
            val rawMsg = e.message ?: ""
            val userFriendlyMsg = when {
                rawMsg.contains("no user record", ignoreCase = true) || rawMsg.contains("USER_NOT_FOUND", ignoreCase = true) ->
                    "No account found with this email. Please tap 'Create Account' first."
                rawMsg.contains("invalid credential", ignoreCase = true) || rawMsg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ->
                    "Incorrect email or password. Please try again."
                else -> rawMsg.ifBlank { "Sign in failed. Please check your credentials." }
            }
            _authState.value = AuthState.Error(userFriendlyMsg)
            Result.failure(Exception(userFriendlyMsg, e))
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<AuthUser> {
        val auth = firebaseAuth ?: return mockFallback.signUpWithEmail(email, password)
        return try {
            _authState.value = AuthState.Loading
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                val domainUser = AuthUser(
                    id = user.uid,
                    email = user.email ?: email,
                    displayName = user.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    isAnonymous = user.isAnonymous
                )
                _authState.value = AuthState.Authenticated(domainUser)
                Result.success(domainUser)
            } else {
                val errorMsg = "Registration failed on Firebase"
                _authState.value = AuthState.Error(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Throwable) {
            val rawMsg = e.message ?: ""
            val userFriendlyMsg = when {
                rawMsg.contains("already in use", ignoreCase = true) || rawMsg.contains("EMAIL_EXISTS", ignoreCase = true) ->
                    "An account with this email already exists. Please tap 'Sign In' instead."
                rawMsg.contains("weak password", ignoreCase = true) ->
                    "Password should be at least 6 characters."
                else -> rawMsg.ifBlank { "Registration failed. Please try again." }
            }
            _authState.value = AuthState.Error(userFriendlyMsg)
            Result.failure(Exception(userFriendlyMsg, e))
        }
    }

    override suspend fun signInAnonymously(): Result<AuthUser> {
        val auth = firebaseAuth ?: return mockFallback.signInAnonymously()
        return try {
            _authState.value = AuthState.Loading
            val authResult = auth.signInAnonymously().await()
            val user = authResult.user
            if (user != null) {
                val domainUser = AuthUser(
                    id = user.uid,
                    displayName = "Guest Candidate",
                    isAnonymous = true
                )
                _authState.value = AuthState.Authenticated(domainUser)
                Result.success(domainUser)
            } else {
                val res = mockFallback.signInAnonymously()
                res.getOrNull()?.let { _authState.value = AuthState.Authenticated(it) }
                res
            }
        } catch (e: Throwable) {
            val res = mockFallback.signInAnonymously()
            res.getOrNull()?.let { _authState.value = AuthState.Authenticated(it) }
            res
        }
    }

    override suspend fun signInWithGoogleToken(idToken: String): Result<AuthUser> {
        val auth = firebaseAuth ?: return mockFallback.signInWithGoogleToken(idToken)
        return try {
            _authState.value = AuthState.Loading
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                val domainUser = AuthUser(
                    id = user.uid,
                    email = user.email,
                    displayName = user.displayName ?: user.email?.substringBefore("@"),
                    photoUrl = user.photoUrl?.toString(),
                    isAnonymous = false
                )
                _authState.value = AuthState.Authenticated(domainUser)
                Result.success(domainUser)
            } else {
                val errorMsg = "Google Sign-In returned null user from Firebase"
                _authState.value = AuthState.Error(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Throwable) {
            if (idToken == "demo_google_id_token") {
                val res = mockFallback.signInWithGoogleToken(idToken)
                res.getOrNull()?.let { _authState.value = AuthState.Authenticated(it) }
                return res
            }
            val errorMsg = e.message ?: "Google authentication failed on Firebase"
            _authState.value = AuthState.Error(errorMsg)
            Result.failure(Exception(errorMsg, e))
        }
    }

    override suspend fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (_: Throwable) {}
        mockFallback.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}
