package com.example.dsarecall.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dsarecall.ui.analytics.AnalyticsScreen
import com.example.dsarecall.ui.analytics.AnalyticsViewModel
import com.example.dsarecall.ui.auth.AuthScreen
import com.example.dsarecall.ui.auth.AuthViewModel
import com.example.dsarecall.ui.bank.ProblemBankScreen
import com.example.dsarecall.ui.bank.ProblemBankViewModel
import com.example.dsarecall.ui.common.SyncStatusIndicator
import com.example.dsarecall.ui.onboarding.OnboardingScreen
import com.example.dsarecall.ui.onboarding.OnboardingViewModel
import com.example.dsarecall.ui.queue.DailyQueueScreen
import com.example.dsarecall.ui.queue.DailyQueueViewModel
import com.example.dsarecall.ui.theme.MinimalistBackground
import com.example.dsarecall.ui.theme.MinimalistSurface
import com.example.dsarecall.ui.theme.SapphirePrimary
import com.example.dsarecall.ui.theme.TextMuted

import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.dsarecall.ui.theme.MauvePrimary
import com.example.dsarecall.ui.theme.ObsidianGlassCard
import com.example.dsarecall.ui.theme.ObsidianVoid
import com.example.dsarecall.ui.theme.PaleLilac
import com.example.dsarecall.ui.theme.SoftLavender
import kotlinx.coroutines.launch

import com.example.dsarecall.domain.model.AuthState
import com.example.dsarecall.ui.auth.ProfileSidebarDrawer

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Onboarding : Screen("onboarding", "Setup", Icons.Default.School)
    object Queue : Screen("daily_queue", "Daily Queue", Icons.Default.Psychology)
    object Bank : Screen("problem_bank", "Problem Bank", Icons.Default.ListAlt)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Analytics)
    object Auth : Screen("auth", "Account", Icons.Default.AccountCircle)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppNavigation(
    onboardingViewModel: OnboardingViewModel,
    queueViewModel: DailyQueueViewModel,
    bankViewModel: ProblemBankViewModel,
    analyticsViewModel: AnalyticsViewModel,
    authViewModel: AuthViewModel? = null,
    hasCompletedOnboarding: Boolean,
    navController: NavHostController = rememberNavController()
) {
    val bottomBarItems = listOf(Screen.Queue, Screen.Bank, Screen.Analytics)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Screen.Onboarding.route && currentRoute != Screen.Auth.route

    val authState = authViewModel?.authState?.collectAsState()?.value
    val syncStatus = authViewModel?.syncStatus?.collectAsState()?.value
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute != Screen.Auth.route && currentRoute != Screen.Onboarding.route,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF0D0B12),
                modifier = Modifier.width(340.dp)
            ) {
                authViewModel?.let { vm ->
                    ProfileSidebarDrawer(
                        viewModel = vm,
                        onSignOut = {
                            coroutineScope.launch {
                                drawerState.close()
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (showBottomBar && authViewModel != null) {
                    TopAppBar(
                        title = {
                            Text(
                                text = "DSA Recall",
                                style = MaterialTheme.typography.titleMedium,
                                color = PaleLilac,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        actions = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile & Account Sidebar",
                                    tint = MauvePrimary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianVoid)
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = ObsidianGlassCard,
                        tonalElevation = androidx.compose.ui.unit.Dp.Unspecified
                    ) {
                        bottomBarItems.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            NavigationBarItem(
                                icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                                label = { Text(screen.title) },
                                selected = isSelected,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MauvePrimary,
                                    selectedTextColor = MauvePrimary,
                                    indicatorColor = Color(0xFF251F33),
                                    unselectedIconColor = SoftLavender.copy(alpha = 0.5f),
                                    unselectedTextColor = SoftLavender.copy(alpha = 0.5f)
                                ),
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            },
            containerColor = ObsidianVoid
        ) { innerPadding ->
            val startDestination = when {
                !hasCompletedOnboarding -> Screen.Onboarding.route
                authState !is AuthState.Authenticated -> Screen.Auth.route
                else -> Screen.Queue.route
            }

            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        viewModel = onboardingViewModel,
                        onCompleteOnboarding = {
                            navController.navigate(Screen.Queue.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Auth.route) {
                    authViewModel?.let { vm ->
                        AuthScreen(
                            viewModel = vm,
                            onAuthSuccess = {
                                navController.navigate(Screen.Queue.route) {
                                    popUpTo(Screen.Auth.route) { inclusive = true }
                                }
                            }
                        )
                    }
                }
                composable(Screen.Queue.route) {
                    DailyQueueScreen(viewModel = queueViewModel)
                }
                composable(Screen.Bank.route) {
                    ProblemBankScreen(viewModel = bankViewModel)
                }
                composable(Screen.Analytics.route) {
                    AnalyticsScreen(viewModel = analyticsViewModel)
                }
            }
        }
    }
}
