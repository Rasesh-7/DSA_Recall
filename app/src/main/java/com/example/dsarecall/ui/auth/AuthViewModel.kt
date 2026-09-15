package com.example.dsarecall.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsarecall.domain.model.AuthState
import com.example.dsarecall.domain.model.SyncStatus
import com.example.dsarecall.domain.repository.AuthRepository
import com.example.dsarecall.domain.sync.SyncEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val syncEngine: SyncEngine
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState
    val syncStatus: StateFlow<SyncStatus> = syncEngine.syncStatus

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isSignUp = MutableStateFlow(false)
    val isSignUp: StateFlow<Boolean> = _isSignUp.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
        _errorMessage.value = null
        _infoMessage.value = null
    }

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
        _errorMessage.value = null
        _infoMessage.value = null
    }

    fun toggleAuthMode() {
        _isSignUp.value = !_isSignUp.value
        _errorMessage.value = null
        _infoMessage.value = null
    }

    fun sendForgotPasswordReset() {
        val mail = _email.value.trim()
        if (mail.isBlank()) {
            _errorMessage.value = "Enter your email address to receive a password reset link"
            _infoMessage.value = null
            return
        }
        _errorMessage.value = null
        _infoMessage.value = "Password reset instructions sent to $mail"
    }

    fun submitEmailAuth() {
        viewModelScope.launch {
            val mail = _email.value.trim()
            val pass = _password.value.trim()

            if (mail.isBlank()) {
                _errorMessage.value = "Please enter an email address"
                return@launch
            }
            if (pass.length < 6) {
                _errorMessage.value = "Password must be at least 6 characters"
                return@launch
            }

            val result = if (_isSignUp.value) {
                authRepository.signUpWithEmail(mail, pass)
            } else {
                authRepository.signInWithEmail(mail, pass)
            }

            result.fold(
                onSuccess = { user ->
                    syncEngine.performSync(user.id)
                },
                onFailure = { ex ->
                    _errorMessage.value = ex.message ?: "Authentication failed"
                }
            )
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            authRepository.signInAnonymously().onSuccess { user ->
                syncEngine.performSync(user.id)
            }
        }
    }

    fun setErrorMessage(msg: String) {
        _errorMessage.value = msg
    }

    fun signInWithGoogleToken(idToken: String) {
        viewModelScope.launch {
            authRepository.signInWithGoogleToken(idToken).fold(
                onSuccess = { user ->
                    syncEngine.performSync(user.id)
                },
                onFailure = { ex ->
                    _errorMessage.value = ex.message ?: "Google authentication failed"
                }
            )
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                syncEngine.performSync(currentUser.id)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
