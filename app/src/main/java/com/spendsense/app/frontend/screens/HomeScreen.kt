package com.spendsense.app.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.spendsense.app.backend.local.ExpenseEntity
import com.spendsense.app.frontend.components.CustomProgressBar
import com.spendsense.app.frontend.components.StandardCard
import com.spendsense.app.frontend.theme.*
import com.spendsense.app.frontend.viewmodels.DashboardState
import com.spendsense.app.frontend.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val currency = userProfile?.currencySymbol ?: "₹"

    Scaffold(
        containerColor = BackgroundGray,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEntry.createRoute()) },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Filled.Add, "Add Entry", modifier = Modifier.size(32.dp))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Good Morning,",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = userProfile?.name ?: "SpendSense User",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.Settings.route) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(PrimaryVariant, CircleShape)
                    ) {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = PrimaryBlue
                        )
                    }
                }
            }

            // Wallet Balance Card
            item {
                StandardCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = PrimaryBlue
                ) {
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.labelLarge,
                        color = SurfaceWhite.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$currency${String.format("%.2f", dashboardState.balance)}",
                        style = MaterialTheme.typography.displayMedium,
                        color = SurfaceWhite,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryItem(
                            title = "Income",
                            amount = "$currency${String.format("%.0f", dashboardState.totalIncome)}",
                            icon = Icons.Default.ArrowDownward,
                            color = AccentGreen
                        )
                        VerticalDivider(color = SurfaceWhite.copy(alpha = 0.1f), modifier = Modifier.height(40.dp))
                        SummaryItem(
                            title = "Expenses",
                            amount = "$currency${String.format("%.0f", dashboardState.totalExpense)}",
                            icon = Icons.Default.ArrowUpward,
                            color = Color.White
                        )
                    }
                }
            }

            // Budget Insight
            item {
                StandardCard(modifier = Modifier.fillMaxWidth()) {
                    val progress = if (dashboardState.budgetLimit > 0) (dashboardState.totalExpense / dashboardState.budgetLimit).toFloat() else 0f
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Monthly Budget", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$currency${dashboardState.totalExpense.toInt()} / $currency${dashboardState.budgetLimit.toInt()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomProgressBar(
                        progress = progress,
                        color = if (progress > 0.9f) WarningRed else PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (progress > 1f) "You've exceeded your budget!" else "${(progress * 100).toInt()}% of budget used",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (progress > 0.9f) WarningRed else TextSecondary
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { navController.navigate(Screen.History.route) }) {
                        Text("View All", color = PrimaryBlue)
                    }
                }
            }

            items(dashboardState.recentExpenses) { expense ->
                TransactionCard(expense, currency)
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun SummaryItem(title: String, amount: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(6.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, color = SurfaceWhite.copy(alpha = 0.7f))
            Text(amount, style = MaterialTheme.typography.titleMedium, color = SurfaceWhite, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TransactionCard(expense: ExpenseEntity, currency: String) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(expense.date))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = BackgroundGray
            ) {
                Icon(
                    imageVector = when(expense.category) {
                        "Food" -> Icons.Default.Restaurant
                        "Shopping" -> Icons.Default.ShoppingBag
                        "Travel" -> Icons.Default.DirectionsBus
                        "Rent" -> Icons.Default.Home
                        "Health" -> Icons.Default.MedicalServices
                        else -> Icons.Default.Payments
                    },
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.category, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
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
