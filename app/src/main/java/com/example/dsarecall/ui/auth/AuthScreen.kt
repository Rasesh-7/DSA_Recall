package com.example.dsarecall.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsarecall.domain.model.AuthState
import com.example.dsarecall.domain.model.AuthUser
import com.example.dsarecall.domain.model.SyncStatus
import kotlinx.coroutines.launch

// Obsidian Mauve Design System Tokens (From Stitch Spec)
private val ObsidianVoid = Color(0xFF0D0B12)
private val ObsidianGlassCard = Color(0xFF191522)
private val MauvePrimary = Color(0xFFA47CA5)
private val MauvePrimaryPressed = Color(0xFF8E6690)
private val SoftLavender = Color(0xFFD8C8FF)
private val PaleLilac = Color(0xFFE8DEFF)
private val GlowingGreen = Color(0xFFA3E635)
private val GlassBorderLight = Color(0x26FFFFFF)
private val GlassBorderMauve = Color(0x40A47CA5)

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isSignUp by viewModel.isSignUp.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val infoMessage by viewModel.infoMessage.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isPasswordVisible by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        // Ambient Radial Background Glows
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MauvePrimary.copy(alpha = 0.18f),
                            SoftLavender.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        radius = 1200f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header Bar & Live Cloud Sync Status Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DSA Recall Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PaleLilac
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ObsidianAuthFormCard(
                email = email,
                password = password,
                isSignUp = isSignUp,
                isPasswordVisible = isPasswordVisible,
                errorMessage = errorMessage,
                infoMessage = infoMessage,
                isLoading = authState is AuthState.Loading,
                onEmailChange = { viewModel.onEmailChanged(it) },
                onPasswordChange = { viewModel.onPasswordChanged(it) },
                onPasswordVisibilityToggle = { isPasswordVisible = !isPasswordVisible },
                onForgotPassword = { viewModel.sendForgotPasswordReset() },
                onToggleAuthMode = { viewModel.toggleAuthMode() },
                onSubmit = { viewModel.submitEmailAuth() },
                onGoogleSignIn = {
                    coroutineScope.launch {
                        GoogleSignInHelper.launchGoogleSignIn(
                            context = context,
                            onSuccess = { idToken ->
                                viewModel.signInWithGoogleToken(idToken)
                            },
                            onFailure = { ex ->
                                viewModel.setErrorMessage(ex.message ?: "Google Sign-In failed")
                            }
                        )
                    }
                },
                onGuestSignIn = {
                    viewModel.signInAnonymously()
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Encryption & Security Guarantee Footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color(0xFF171322))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Encrypted",
                    tint = SoftLavender,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "End-to-end encrypted problem state & review schedules",
                    fontSize = 11.sp,
                    color = SoftLavender.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ProfileSidebarDrawer(
    viewModel: AuthViewModel,
    onSignOut: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isSignUp by viewModel.isSignUp.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val infoMessage by viewModel.infoMessage.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile & Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PaleLilac
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (authState is AuthState.Authenticated) {
                val user = (authState as AuthState.Authenticated).user
                ObsidianUserProfileCard(
                    user = user,
                    onSignOut = {
                        coroutineScope.launch {
                            GoogleSignInHelper.clearCredentialState(context)
                        }
                        viewModel.signOut()
                        onSignOut()
                    },
                    onSwitchAccount = {
                        coroutineScope.launch {
                            GoogleSignInHelper.clearCredentialState(context)
                        }
                        viewModel.signOut()
                        onSignOut()
                    }
                )
            } else {
                ObsidianAuthFormCard(
                    email = email,
                    password = password,
                    isSignUp = isSignUp,
                    isPasswordVisible = isPasswordVisible,
                    errorMessage = errorMessage,
                    infoMessage = infoMessage,
                    isLoading = authState is AuthState.Loading,
                    onEmailChange = { viewModel.onEmailChanged(it) },
                    onPasswordChange = { viewModel.onPasswordChanged(it) },
                    onPasswordVisibilityToggle = { isPasswordVisible = !isPasswordVisible },
                    onForgotPassword = { viewModel.sendForgotPasswordReset() },
                    onToggleAuthMode = { viewModel.toggleAuthMode() },
                    onSubmit = { viewModel.submitEmailAuth() },
                    onGoogleSignIn = {
                        coroutineScope.launch {
                            GoogleSignInHelper.launchGoogleSignIn(
                                context = context,
                                onSuccess = { idToken ->
                                    viewModel.signInWithGoogleToken(idToken)
                                },
                                onFailure = { ex ->
                                    viewModel.setErrorMessage(ex.message ?: "Google Sign-In failed")
                                }
                            )
                        }
                    },
                    onGuestSignIn = {
                        viewModel.signInAnonymously()
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color(0xFF171322))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Encrypted",
                    tint = SoftLavender,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "End-to-end encrypted problem state & review schedules",
                    fontSize = 11.sp,
                    color = SoftLavender.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ObsidianUserProfileCard(
    user: AuthUser,
    onSignOut: () -> Unit,
    onSwitchAccount: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = ObsidianGlassCard.copy(alpha = 0.85f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(GlassBorderLight, GlassBorderMauve)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), spotColor = MauvePrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MauvePrimary.copy(alpha = 0.35f),
                                ObsidianVoid
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(SoftLavender, MauvePrimary)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User Avatar",
                    tint = SoftLavender,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user.displayName ?: user.email ?: "Guest Candidate",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PaleLilac
            )

            if (!user.email.isNull_or_empty_ext()) {
                Text(
                    text = user.email ?: "",
                    fontSize = 14.sp,
                    color = SoftLavender.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = if (user.isAnonymous) Color(0xFF251F33) else MauvePrimary.copy(alpha = 0.2f),
                modifier = Modifier
                    .padding(top = 12.dp)
                    .border(
                        width = 1.dp,
                        color = if (user.isAnonymous) Color.White.copy(alpha = 0.1f) else MauvePrimary.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(9999.dp)
                    )
            ) {
                Text(
                    text = if (user.isAnonymous) "Local Guest Mode" else "Firebase Cloud Synced",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (user.isAnonymous) SoftLavender.copy(alpha = 0.8f) else PaleLilac,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic Daily Queue Pace Selector
            val context = androidx.compose.ui.platform.LocalContext.current
            val onboardingRepo = remember { com.example.dsarecall.data.repository.OnboardingRepository(context) }
            var currentQuota by remember { mutableStateOf(onboardingRepo.getDailyQuota()) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF221C2E).copy(alpha = 0.6f))
                    .border(1.dp, GlassBorderLight, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Daily Queue Pace Goal",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PaleLilac
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        3 to "🟢 3/day",
                        5 to "🟣 5/day",
                        10 to "⚡ 10/day"
                    ).forEach { (quota, label) ->
                        val isSelected = currentQuota == quota
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MauvePrimary else Color(0xFF191522))
                                .border(1.dp, if (isSelected) MauvePrimary else GlassBorderLight, RoundedCornerShape(8.dp))
                                .clickable {
                                    currentQuota = quota
                                    onboardingRepo.saveDailyQuota(quota)
                                }
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) ObsidianVoid else PaleLilac
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onSwitchAccount,
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, SoftLavender.copy(alpha = 0.3f), RoundedCornerShape(9999.dp))
            ) {
                Text("Sign In with Different Account", color = PaleLilac, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFF87171))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sign Out", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ObsidianAuthFormCard(
    email: String,
    password: String,
    isSignUp: Boolean,
    isPasswordVisible: Boolean,
    errorMessage: String?,
    infoMessage: String?,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onForgotPassword: () -> Unit,
    onToggleAuthMode: () -> Unit,
    onSubmit: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onGuestSignIn: () -> Unit
) {
    // App Brand Logo Emblem (From Stitch Spec)
    Box(
        modifier = Modifier
            .size(84.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MauvePrimary.copy(alpha = 0.4f),
                        ObsidianVoid
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(SoftLavender, MauvePrimary)
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = "DSA Recall Logo",
            tint = SoftLavender,
            modifier = Modifier.size(52.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = if (isSignUp) "Create Account" else "Welcome to DSA Recall",
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = PaleLilac
    )

    Text(
        text = "Sync your DSA problem recall progress across all your devices",
        fontSize = 14.sp,
        color = SoftLavender.copy(alpha = 0.75f),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
    )

    // Frosted Glassmorphic Auth Card Container (24dp rounded corners)
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = ObsidianGlassCard.copy(alpha = 0.85f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(GlassBorderLight, GlassBorderMauve)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), spotColor = MauvePrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Outlined Email Field
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email Address", color = SoftLavender.copy(alpha = 0.8f)) },
                placeholder = { Text("developer@domain.com", color = SoftLavender.copy(alpha = 0.4f)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = MauvePrimary
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MauvePrimary,
                    unfocusedBorderColor = GlassBorderLight,
                    focusedContainerColor = Color(0xFF221C2E).copy(alpha = 0.6f),
                    unfocusedContainerColor = Color(0xFF221C2E).copy(alpha = 0.3f),
                    focusedTextColor = PaleLilac,
                    unfocusedTextColor = PaleLilac
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Outlined Password Field
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password", color = SoftLavender.copy(alpha = 0.8f)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MauvePrimary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onPasswordVisibilityToggle) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = SoftLavender.copy(alpha = 0.7f)
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MauvePrimary,
                    unfocusedBorderColor = GlassBorderLight,
                    focusedContainerColor = Color(0xFF221C2E).copy(alpha = 0.6f),
                    unfocusedContainerColor = Color(0xFF221C2E).copy(alpha = 0.3f),
                    focusedTextColor = PaleLilac,
                    unfocusedTextColor = PaleLilac
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Right-aligned "Forgot password?" Link
            if (!isSignUp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onForgotPassword,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "Forgot password?",
                            color = SoftLavender,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Info Message (e.g. Password Reset Sent)
            AnimatedVisibility(visible = infoMessage != null) {
                infoMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = GlowingGreen,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // Error Message Banner
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color(0xFFF87171),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Mauve Sign In / Sign Up Pill CTA Button
            Button(
                onClick = onSubmit,
                enabled = !isLoading,
                shape = RoundedCornerShape(9999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MauvePrimary,
                    disabledContainerColor = MauvePrimaryPressed
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(9999.dp), spotColor = MauvePrimary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = ObsidianVoid,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isSignUp) "Sign Up" else "Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ObsidianVoid
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Toggle Auth Mode Text Button
            TextButton(onClick = onToggleAuthMode) {
                Text(
                    text = if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                    color = SoftLavender,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // OR Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.12f)
                )
                Text(
                    text = "OR",
                    fontSize = 12.sp,
                    color = SoftLavender.copy(alpha = 0.5f),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.12f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Google Sign In Glass Pill Button with Multi-Colored Google 'G' Symbol
            OutlinedButton(
                onClick = onGoogleSignIn,
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0xFF251F33).copy(alpha = 0.6f), RoundedCornerShape(9999.dp))
                    .border(1.dp, SoftLavender.copy(alpha = 0.25f), RoundedCornerShape(9999.dp))
            ) {
                GoogleLogoIcon(modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Continue with Google", color = PaleLilac, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Continue as Guest (Local Offline Backup) Glass Pill Button
            OutlinedButton(
                onClick = onGuestSignIn,
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0xFF251F33).copy(alpha = 0.6f), RoundedCornerShape(9999.dp))
                    .border(1.dp, SoftLavender.copy(alpha = 0.25f), RoundedCornerShape(9999.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MauvePrimary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Continue as Guest (Local Offline)",
                    color = PaleLilac,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Authentic Multi-Colored Google 'G' Logo Canvas Composable
 */
@Composable
private fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)

        // Red top arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 190f,
            sweepAngle = 100f,
            useCenter = true,
            size = Size(width, height)
        )
        // Yellow bottom-left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 120f,
            sweepAngle = 70f,
            useCenter = true,
            size = Size(width, height)
        )
        // Green bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 30f,
            sweepAngle = 90f,
            useCenter = true,
            size = Size(width, height)
        )
        // Blue right arc
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -40f,
            sweepAngle = 70f,
            useCenter = true,
            size = Size(width, height)
        )
        // Inner mask
        drawCircle(
            color = Color(0xFF251F33),
            radius = width * 0.28f,
            center = center
        )
        // Right horizontal bar
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(center.x, center.y - height * 0.12f),
            size = Size(width * 0.48f, height * 0.24f)
        )
    }
}

@Composable
private fun ObsidianSyncBadge(
    syncStatus: SyncStatus,
    onSyncClick: () -> Unit
) {
    val (badgeBg, dotColor, label, isSpinning) = when (syncStatus) {
        is SyncStatus.Syncing -> Quadruple(SoftLavender.copy(alpha = 0.12f), SoftLavender, "Syncing...", true)
        is SyncStatus.Synced -> Quadruple(GlowingGreen.copy(alpha = 0.12f), GlowingGreen, "Cloud Synced", false)
        is SyncStatus.Error -> Quadruple(Color(0xFFF87171).copy(alpha = 0.12f), Color(0xFFF87171), "Sync Error", false)
        is SyncStatus.Idle -> Quadruple(Color.White.copy(alpha = 0.08f), SoftLavender.copy(alpha = 0.6f), "Offline Backup", false)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(badgeBg)
            .border(1.dp, dotColor.copy(alpha = 0.3f), RoundedCornerShape(9999.dp))
            .clickable { onSyncClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isSpinning) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                color = dotColor,
                strokeWidth = 2.dp
            )
        } else {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = PaleLilac
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun String?.isNull_or_empty_ext(): Boolean = this == null || this.trim().isEmpty()
