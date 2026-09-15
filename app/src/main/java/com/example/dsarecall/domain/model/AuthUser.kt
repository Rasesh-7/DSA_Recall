package com.example.dsarecall.domain.model

data class AuthUser(
    val id: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false
)
