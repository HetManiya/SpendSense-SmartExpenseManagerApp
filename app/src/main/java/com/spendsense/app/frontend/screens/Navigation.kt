package com.spendsense.app.frontend.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spendsense.app.frontend.theme.SpendSenseTheme
import com.spendsense.app.frontend.viewmodels.AuthViewModel
import com.spendsense.app.frontend.viewmodels.MainViewModel
import com.spendsense.app.frontend.viewmodels.OnboardingViewModel
import com.spendsense.app.frontend.viewmodels.ProfileViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Splash : Screen("splash", "Splash", null)
    object Auth : Screen("auth", "Authentication", null)
    object Onboarding : Screen("onboarding", "Profile Setup", null)
    object Home : Screen("home", "Home", Icons.Rounded.Home)
    object History : Screen("history", "History", Icons.Rounded.History)
    object Analytics : Screen("analytics", "Analysis", Icons.Rounded.BarChart)
    object Goals : Screen("goals", "Targets", Icons.Rounded.TrackChanges)
    object Reports : Screen("reports", "Reports", Icons.Rounded.Summarize)
    object Profile : Screen("profile", "Profile", null)
    object Settings : Screen("settings", "Settings", null)
    object GroupSettings : Screen("group_settings", "Group Sync", null)
    object AddEntry : Screen("add_entry?expenseId={expenseId}", "Add Entry", null) {
        fun createRoute(expenseId: Int? = null) = if (expenseId != null) "add_entry?expenseId=$expenseId" else "add_entry"
    }
}

@Composable
fun SpendSenseNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = Screen.Splash.route, modifier = modifier) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController, viewModel = mainViewModel)
        }
        composable(Screen.Auth.route) {
            AuthScreen(
                navController = navController,
                viewModel = mainViewModel,
                onSkip = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Onboarding.route) {
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val authViewModel: AuthViewModel = hiltViewModel()
            
            OnboardingFlow(
                navController = navController,
                viewModel = onboardingViewModel,
                onFinish = {
                    val state = onboardingViewModel.uiState.value
                    profileViewModel.saveUserProfile(
                        name = state.username,
                        currency = "₹",
                        budgetLimit = state.budget.toDoubleOrNull() ?: 0.0,
                        isGuest = authViewModel.isGuest.value
                    )
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController, viewModel = mainViewModel)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController, viewModel = mainViewModel)
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen(navController = navController, viewModel = mainViewModel)
        }
        composable(Screen.Goals.route) {
            GoalsScreen(navController = navController, viewModel = mainViewModel)
        }
        composable(Screen.Reports.route) {
            ReportsScreen(navController = navController, viewModel = mainViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController, viewModel = mainViewModel)
        }
        composable(Screen.Settings.route) {
            ProfileScreen(navController = navController, viewModel = mainViewModel)
        }
        composable(Screen.GroupSettings.route) {
            GroupSettingsScreen(navController = navController, viewModel = mainViewModel)
        }
        composable(
            route = Screen.AddEntry.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getInt("expenseId") ?: -1
            AddEntryScreen(navController = navController, viewModel = mainViewModel, expenseId = expenseId)
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    MainContent(
        currentRoute = currentRoute,
        navController = navController,
        viewModel = viewModel
    )
}

@Composable
fun MainContent(
    currentRoute: String?,
    navController: NavHostController,
    viewModel: MainViewModel? = null
) {
    val bottomBarScreens = listOf(
        Screen.Home,
        Screen.History,
        Screen.Analytics,
        Screen.Goals,
        Screen.Reports
    )

    Scaffold(
        bottomBar = {
            if (bottomBarScreens.any { it.route == currentRoute } || currentRoute?.startsWith("add_entry") == true || currentRoute == Screen.Profile.route) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp
                ) {
                    bottomBarScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    imageVector = screen.icon!!, 
                                    contentDescription = screen.title
                                ) 
                            },
                            label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (viewModel != null) {
            SpendSenseNavHost(navController, viewModel, Modifier.padding(innerPadding))
        }
    }
}
