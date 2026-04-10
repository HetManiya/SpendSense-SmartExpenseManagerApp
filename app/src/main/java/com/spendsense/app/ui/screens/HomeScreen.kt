package com.spendsense.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spendsense.app.data.local.ExpenseEntity
import com.spendsense.app.ui.components.CustomProgressBar
import com.spendsense.app.ui.components.StandardCard
import com.spendsense.app.ui.theme.*
import com.spendsense.app.ui.viewmodels.DashboardState
import com.spendsense.app.ui.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val currency = userProfile?.currencySymbol ?: "$"

    HomeContent(
        dashboardState = dashboardState,
        currency = currency,
        onAddClick = { navController.navigate(Screen.AddEntry.createRoute()) }
    )
}

@Composable
fun HomeContent(
    dashboardState: DashboardState,
    currency: String,
    onAddClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Add Entry")
            }
        },
        containerColor = BackgroundGray
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome back!",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Balance Summary Card
            item {
                StandardCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "$currency${String.format("%.2f", dashboardState.balance)}",
                        style = MaterialTheme.typography.displayMedium,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SummaryMiniItem(
                            title = "Income",
                            amount = "$currency${String.format("%.2f", dashboardState.totalIncome)}",
                            icon = Icons.Default.ArrowUpward,
                            color = AccentGreen
                        )
                        SummaryMiniItem(
                            title = "Expenses",
                            amount = "$currency${String.format("%.2f", dashboardState.totalExpense)}",
                            icon = Icons.Default.ArrowDownward,
                            color = WarningRed
                        )
                    }
                }
            }

            // Budget Progress
            item {
                StandardCard(modifier = Modifier.fillMaxWidth()) {
                    val progress = if (dashboardState.budgetLimit > 0) (dashboardState.totalExpense / dashboardState.budgetLimit).toFloat() else 0f
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Monthly Budget", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = when {
                                progress < 0.8f -> PrimaryBlue
                                progress < 1.0f -> Color(0xFFFFA500)
                                else -> WarningRed
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    CustomProgressBar(
                        progress = progress,
                        color = when {
                            progress < 0.8f -> PrimaryBlue
                            progress < 1.0f -> Color(0xFFFFA500)
                            else -> WarningRed
                        }
                    )
                    
                    if (progress >= 0.9f) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Approaching limit!",
                                color = WarningRed,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Limit: $currency${dashboardState.budgetLimit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            item {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            items(dashboardState.recentExpenses) { expense ->
                TransactionItem(expense, currency)
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SummaryMiniItem(title: String, amount: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Text(amount, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TransactionItem(expense: ExpenseEntity, currency: String) {
    val dateStr = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(expense.date))
    StandardCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(expense.category.take(1).uppercase(), color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.category, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(dateStr, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Text(
                text = "-$currency${String.format("%.2f", expense.amount)}",
                style = MaterialTheme.typography.titleMedium,
                color = WarningRed,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SpendSenseTheme {
        HomeContent(
            dashboardState = DashboardState(
                totalIncome = 5000.0,
                totalExpense = 1200.0,
                balance = 3800.0,
                budgetLimit = 2000.0,
                recentExpenses = listOf(
                    ExpenseEntity(1, 50.0, "Food", System.currentTimeMillis(), "Lunch", "Cash"),
                    ExpenseEntity(2, 20.0, "Transport", System.currentTimeMillis(), "Bus", "Card")
                )
            ),
            currency = "$",
            onAddClick = {}
        )
    }
}
