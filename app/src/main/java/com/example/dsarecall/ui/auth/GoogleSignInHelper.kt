package com.example.dsarecall.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID

object GoogleSignInHelper {
    suspend fun clearCredentialState(context: Context) {
        try {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
        } catch (_: Exception) {}
    }

    suspend fun launchGoogleSignIn(
        context: Context,
        webClientId: String = "1096430490457-mhlfh22veqo3kpuuqcjo6h4f2hn717bv.apps.googleusercontent.com",
        onSuccess: (idToken: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)
        
        // Force clear cached credential state so Google email selection popup appears every time
        try {
            credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
        } catch (_: Exception) {}

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )
            val credential = result.credential
            if (credential is GoogleIdTokenCredential) {
                onSuccess(credential.idToken)
            } else if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    onSuccess(googleIdTokenCredential.idToken)
                } catch (e: Exception) {
                    val idToken = credential.data.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")
                    if (!idToken.isNullOrEmpty()) {
                        onSuccess(idToken)
                    } else {
                        onFailure(e)
                    }
                }
            } else {
                onFailure(Exception("Unrecognized Google Credential type"))
            }
        } catch (e: GetCredentialException) {
            onFailure(e)
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}
