package com.example.dsarecall.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dsarecall.ui.analytics.AnalyticsScreen
import com.example.dsarecall.ui.analytics.AnalyticsViewModel
import com.example.dsarecall.ui.bank.ProblemBankScreen
import com.example.dsarecall.ui.bank.ProblemBankViewModel
import com.example.dsarecall.ui.onboarding.OnboardingScreen
import com.example.dsarecall.ui.onboarding.OnboardingViewModel
import com.example.dsarecall.ui.queue.DailyQueueScreen
import com.example.dsarecall.ui.queue.DailyQueueViewModel
import com.example.dsarecall.ui.theme.MinimalistBackground
import com.example.dsarecall.ui.theme.MinimalistSurface
import com.example.dsarecall.ui.theme.SapphirePrimary
import com.example.dsarecall.ui.theme.TextMuted

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Onboarding : Screen("onboarding", "Setup", Icons.Default.School)
    object Queue : Screen("daily_queue", "Daily Queue", Icons.Default.Psychology)
    object Bank : Screen("problem_bank", "Problem Bank", Icons.Default.ListAlt)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Analytics)
}

@Composable
fun MainAppNavigation(
    onboardingViewModel: OnboardingViewModel,
    queueViewModel: DailyQueueViewModel,
    bankViewModel: ProblemBankViewModel,
    analyticsViewModel: AnalyticsViewModel,
    hasCompletedOnboarding: Boolean,
    navController: NavHostController = rememberNavController()
) {
    val bottomBarItems = listOf(Screen.Queue, Screen.Bank, Screen.Analytics)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Screen.Onboarding.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MinimalistSurface,
                    tonalElevation = androidx.compose.ui.unit.Dp.Unspecified
                ) {
                    bottomBarItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SapphirePrimary,
                                selectedTextColor = SapphirePrimary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
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
        containerColor = MinimalistBackground
    ) { innerPadding ->
        val startDestination = if (hasCompletedOnboarding) Screen.Queue.route else Screen.Onboarding.route

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
