package com.spendsense.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spendsense.app.ui.theme.SpendSenseTheme
import com.spendsense.app.ui.viewmodels.MainViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Splash : Screen("splash", "Splash", null)
    object Onboarding : Screen("onboarding", "Profile Setup", null)
    object Home : Screen("home", "Dashboard", Icons.Filled.Home)
    object History : Screen("history", "History", Icons.Filled.History)
    object Analytics : Screen("analytics", "Analytics", Icons.Filled.Analytics)
    object Goals : Screen("goals", "Goals", Icons.Filled.Flag)
    object Reports : Screen("reports", "Reports", Icons.Filled.Assessment)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object AddEntry : Screen("add_entry?expenseId={expenseId}", "Add Entry", null) {
        fun createRoute(expenseId: Int? = null) = if (expenseId != null) "add_entry?expenseId=$expenseId" else "add_entry"
    }
}

@Composable
fun SpendSenseNavHost(navController: NavHostController, viewModel: MainViewModel, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screen.Splash.route, modifier = modifier) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Onboarding.route) {
            ProfileSetupScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Goals.route) {
            GoalsScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Reports.route) {
            ReportsScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            route = Screen.AddEntry.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getInt("expenseId") ?: -1
            AddEntryScreen(navController = navController, viewModel = viewModel, expenseId = expenseId)
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
        Screen.Reports,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            if (bottomBarScreens.any { it.route == currentRoute || currentRoute?.startsWith("add_entry") == true }) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomBarScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
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
                            }
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

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SpendSenseTheme {
        MainContent(
            currentRoute = Screen.Home.route,
            navController = rememberNavController()
        )
    }
}
