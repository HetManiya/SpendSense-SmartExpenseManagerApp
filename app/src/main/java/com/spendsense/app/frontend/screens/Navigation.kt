package com.spendsense.app.frontend.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spendsense.app.frontend.theme.PrimaryBlue
import com.spendsense.app.frontend.theme.TextSecondary
import com.spendsense.app.frontend.viewmodels.AuthViewModel
import com.spendsense.app.frontend.viewmodels.MainViewModel
import com.spendsense.app.frontend.viewmodels.OnboardingViewModel
import com.spendsense.app.frontend.viewmodels.ProfileViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Splash : Screen("splash", "Splash", null)
    object Auth : Screen("auth", "Authentication", null)
    object Onboarding : Screen("onboarding", "Profile Setup", null)
    object Home : Screen("home", "Home", Icons.Outlined.Home)
    object History : Screen("history", "History", Icons.Rounded.History)
    object Analytics : Screen("analytics", "Analysis", Icons.Rounded.BarChart)
    object Goals : Screen("goals", "Targets", Icons.Rounded.TrackChanges)
    object Reports : Screen("reports", "Reports", Icons.Rounded.Description)
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
                    mainViewModel.setGuestMode(true)
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
            SettingsScreen(navController = navController, viewModel = mainViewModel)
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
            if (bottomBarScreens.any { it.route == currentRoute } || currentRoute?.startsWith("add_entry") == true || currentRoute == Screen.Profile.route || currentRoute == Screen.Settings.route) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.2f))
                        .clip(RoundedCornerShape(24.dp)),
                    color = Color.White,
                    tonalElevation = 0.dp
                ) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(72.dp)
                    ) {
                        bottomBarScreens.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                icon = { 
                                    Icon(
                                        imageVector = screen.icon!!, 
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(24.dp)
                                    ) 
                                },
                                label = { 
                                    Text(
                                        text = screen.title, 
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    ) 
                                },
                                selected = selected,
                                alwaysShowLabel = false,
                                onClick = {
                                    if (!selected) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    unselectedIconColor = TextSecondary.copy(alpha = 0.6f),
                                    indicatorColor = PrimaryBlue.copy(alpha = 0.1f),
                                    selectedTextColor = PrimaryBlue,
                                    unselectedTextColor = TextSecondary.copy(alpha = 0.6f)
                                )
                            )
                        }
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
