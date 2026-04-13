package com.spendsense.app.frontend.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spendsense.app.R
import com.spendsense.app.backend.local.ExpenseEntity
import com.spendsense.app.frontend.components.CustomProgressBar
import com.spendsense.app.frontend.components.StandardCard
import com.spendsense.app.frontend.theme.*
import com.spendsense.app.frontend.viewmodels.DashboardState
import com.spendsense.app.frontend.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val groupId by viewModel.groupId.collectAsState()
    val aiInsights by viewModel.aiInsights.collectAsState()
    val currency = userProfile?.currencySymbol ?: "₹"
    
    var showQuickAdd by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.generateAiInsights()
    }

    Scaffold(
        containerColor = BackgroundGray,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAdd = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Rounded.Add, "Quick Add", modifier = Modifier.size(32.dp))
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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.good_morning) + ",",
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
                        onClick = { navController.navigate(Screen.Profile.route) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(PrimaryVariant, CircleShape)
                    ) {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = "Profile",
                            tint = PrimaryBlue
                        )
                    }
                }
            }

            // AI Insight Card
            if (aiInsights.isNotEmpty()) {
                item {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.7f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    StandardCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawCircle(
                                    color = PrimaryBlue.copy(alpha = 0.05f),
                                    radius = size.minDimension,
                                    center = Offset(size.width, 0f)
                                )
                            },
                        containerColor = Color.White
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(PrimaryBlue.copy(alpha = 0.1f * alpha), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(stringResource(R.string.magic_insight), style = MaterialTheme.typography.labelMedium, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                Text(
                                    text = aiInsights.first(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }

            // Wallet Balance Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryBlue, Color(0xFF1A237E)),
                                start = Offset(0f, 0f),
                                end = Offset(1000f, 1000f)
                            )
                        )
                        .padding(28.dp)
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            radius = size.minDimension / 1.5f,
                            center = Offset(size.width * 0.9f, size.height * 0.2f)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.total_balance),
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
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryItem(
                                title = stringResource(R.string.income),
                                amount = "$currency${String.format("%.0f", dashboardState.totalIncome)}",
                                icon = Icons.Rounded.TrendingUp,
                                color = AccentGreen
                            )
                            VerticalDivider(color = SurfaceWhite.copy(alpha = 0.1f), modifier = Modifier.height(40.dp).width(1.dp))
                            SummaryItem(
                                title = stringResource(R.string.expenses),
                                amount = "$currency${String.format("%.0f", dashboardState.totalExpense)}",
                                icon = Icons.Rounded.TrendingDown,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Daily Limit Card
            item {
                val calendar = Calendar.getInstance()
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                val daysRemaining = (daysInMonth - currentDay + 1).coerceAtLeast(1)
                
                val budgetLeft = (dashboardState.budgetLimit - dashboardState.totalExpense).coerceAtLeast(0.0)
                val dailyLimit = budgetLeft / daysRemaining

                StandardCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = PrimaryVariant.copy(alpha = 0.5f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Speed, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(stringResource(R.string.daily_limit), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Text(
                                text = "You can spend $currency${dailyLimit.toInt()} more today",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Budget Insight
            item {
                StandardCard(modifier = Modifier.fillMaxWidth()) {
                    val progress = if (dashboardState.budgetLimit > 0) (dashboardState.totalExpense / dashboardState.budgetLimit).toFloat() else 0f
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.monthly_budget), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$currency${dashboardState.totalExpense.toInt()} / $currency${dashboardState.budgetLimit.toInt()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val barColor = when {
                        progress < 0.6f -> AccentGreen
                        progress < 0.85f -> Color(0xFFFFA500)
                        else -> WarningRed
                    }
                    
                    CustomProgressBar(
                        progress = progress,
                        color = barColor
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
                        text = stringResource(R.string.recent_transactions),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { navController.navigate(Screen.History.route) }) {
                        Text(stringResource(R.string.view_all), color = PrimaryBlue)
                    }
                }
            }

            if (dashboardState.recentExpenses.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.Receipt, contentDescription = null, modifier = Modifier.size(64.dp), tint = DividerGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.no_transactions), color = TextSecondary)
                        Text(stringResource(R.string.tap_to_add), style = MaterialTheme.typography.labelSmall, color = TextSecondary.copy(alpha = 0.6f))
                    }
                }
            } else {
                items(dashboardState.recentExpenses) { expense ->
                    TransactionCard(expense, currency)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showQuickAdd) {
        QuickAddBottomSheet(
            currency = currency,
            onDismiss = { showQuickAdd = false },
            onSaveExpense = { amt, cat ->
                viewModel.addOrUpdateExpense(amount = amt, category = cat, note = "Quick Add", paymentMethod = "Cash")
                showQuickAdd = false
            },
            onSaveIncome = { amt, src ->
                viewModel.addOrUpdateIncome(amount = amt, source = src, note = "Quick Add")
                showQuickAdd = false
            },
            onFullAdd = {
                showQuickAdd = false
                navController.navigate(Screen.AddEntry.createRoute())
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    currency: String,
    onDismiss: () -> Unit,
    onSaveExpense: (Double, String) -> Unit,
    onSaveIncome: (Double, String) -> Unit,
    onFullAdd: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("Food") }
    var selectedSource by remember { mutableStateOf("Salary") }
    
    val categories = listOf("Food", "Travel", "Shopping", "Others")
    val sources = listOf("Salary", "Freelance", "Gift", "Investment")

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = SurfaceWhite) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isExpense) stringResource(R.string.quick_add_expense) else stringResource(R.string.quick_add_income), 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onFullAdd) {
                    Icon(Icons.Rounded.OpenInNew, contentDescription = "Full Entry", tint = PrimaryBlue)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundGray)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isExpense) WarningRed.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { isExpense = true }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Expense", color = if (isExpense) WarningRed else TextSecondary, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isExpense) AccentGreen.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { isExpense = false }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Income", color = if (!isExpense) AccentGreen else TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                label = { Text("Amount ($currency)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isExpense) WarningRed else AccentGreen,
                    focusedLabelColor = if (isExpense) WarningRed else AccentGreen
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Category / Source", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val items = if (isExpense) categories else sources
                items.forEach { item ->
                    val selected = if (isExpense) selectedCategory == item else selectedSource == item
                    FilterChip(
                        selected = selected,
                        onClick = { if (isExpense) selectedCategory = item else selectedSource = item },
                        label = { Text(item) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isExpense) WarningRed else AccentGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        if (isExpense) onSaveExpense(amt, selectedCategory)
                        else onSaveIncome(amt, selectedSource)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isExpense) WarningRed else AccentGreen
                )
            ) {
                Text(stringResource(R.string.save_now), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                        "Food" -> Icons.Rounded.Restaurant
                        "Shopping" -> Icons.Rounded.ShoppingCart
                        "Travel" -> Icons.Rounded.DirectionsBus
                        "Rent" -> Icons.Rounded.Home
                        "Health" -> Icons.Rounded.HealthAndSafety
                        else -> Icons.Rounded.Payments
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
