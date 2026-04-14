package com.spendsense.app.frontend.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
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
import com.spendsense.app.frontend.components.GlassCard
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
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Rounded.Add, "Quick Add", modifier = Modifier.size(32.dp))
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Decorative Background Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(PrimaryBlue.copy(alpha = 0.08f), Color.Transparent)
                        )
                    )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
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
                                text = "Hello,",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = userProfile?.name ?: "SpendSense User",
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-1).sp
                            )
                        }
                        
                        Surface(
                            onClick = { navController.navigate(Screen.Profile.route) },
                            modifier = Modifier.size(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = "Profile",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                // Wallet Balance Card - Enhanced with more modern design
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = PrimaryBlue.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(32.dp),
                        color = PrimaryBlue
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(PrimaryBlue, Color(0xFF1A237E)),
                                        start = Offset(0f, 0f),
                                        end = Offset(1000f, 1000f)
                                    )
                                )
                                .padding(28.dp)
                        ) {
                            // Abstract design elements
                            androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.05f),
                                    radius = size.minDimension / 1.2f,
                                    center = Offset(size.width * 0.95f, size.height * 0.1f)
                                )
                            }

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.total_balance).uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SurfaceWhite.copy(alpha = 0.6f),
                                        letterSpacing = 1.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = Color.White.copy(alpha = 0.2f))
                                }
                                
                                Text(
                                    text = "$currency${String.format("%.2f", dashboardState.balance)}",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = SurfaceWhite,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-1).sp
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
                                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.1f)))
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
                }

                // AI Insight Card - More magical appearance
                if (aiInsights.isNotEmpty()) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = PrimaryBlue.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.AutoAwesome, 
                                            contentDescription = null, 
                                            tint = PrimaryBlue, 
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "SMART INSIGHT", 
                                        style = MaterialTheme.typography.labelMedium, 
                                        color = PrimaryBlue, 
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = aiInsights.first(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        lineHeight = 22.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Budget & Daily Limit - Combined or side-by-side for better space
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Monthly Overview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        StandardCard(modifier = Modifier.fillMaxWidth()) {
                            val progress = if (dashboardState.budgetLimit > 0) (dashboardState.totalExpense / dashboardState.budgetLimit).toFloat() else 0f
                            val barColor = when {
                                progress < 0.6f -> AccentGreen
                                progress < 0.85f -> Color(0xFFFFA500)
                                else -> WarningRed
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Monthly Budget",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = barColor,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            CustomProgressBar(
                                progress = progress,
                                color = barColor
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$currency${dashboardState.totalExpense.toInt()} spent",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Limit: $currency${dashboardState.budgetLimit.toInt()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                            }
                        }

                        val calendar = Calendar.getInstance()
                        val daysRemaining = (calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH) + 1).coerceAtLeast(1)
                        val budgetLeft = (dashboardState.budgetLimit - dashboardState.totalExpense).coerceAtLeast(0.0)
                        val dailyLimit = budgetLeft / daysRemaining

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = PrimaryBlue.copy(alpha = 0.05f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Bolt, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Safe Daily Spend", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                                    Text(
                                        text = "$currency${dailyLimit.toInt()} / day",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PrimaryBlue
                                    )
                                }
                            }
                        }
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
                            Text(stringResource(R.string.view_all), color = PrimaryBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (dashboardState.recentExpenses.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Rounded.ReceiptLong, 
                                contentDescription = null, 
                                modifier = Modifier.size(80.dp).alpha(0.2f), 
                                tint = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No transactions yet", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Text("Start by adding your first expense", style = MaterialTheme.typography.bodySmall, color = TextSecondary.copy(alpha = 0.6f))
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

    ModalBottomSheet(
        onDismissRequest = onDismiss, 
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = DividerGray) }
    ) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isExpense) "Quick Expense" else "Quick Income", 
                    style = MaterialTheme.typography.headlineSmall, 
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Surface(
                    onClick = onFullAdd,
                    shape = CircleShape,
                    color = BackgroundGray,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.OpenInNew, contentDescription = "Full Entry", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BackgroundGray)
                    .padding(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isExpense) WarningRed else Color.Transparent)
                        .clickable { isExpense = true }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Expense", 
                        color = if (isExpense) Color.White else TextSecondary, 
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (!isExpense) AccentGreen else Color.Transparent)
                        .clickable { isExpense = false }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Income", 
                        color = if (!isExpense) Color.White else TextSecondary, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                label = { Text("Amount ($currency)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isExpense) WarningRed else AccentGreen,
                    unfocusedBorderColor = DividerGray
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Select Category", style = MaterialTheme.typography.labelLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val items = if (isExpense) categories else sources
                items.forEach { item ->
                    val selected = if (isExpense) selectedCategory == item else selectedSource == item
                    FilterChip(
                        selected = selected,
                        onClick = { if (isExpense) selectedCategory = item else selectedSource = item },
                        label = { Text(item) },
                        shape = RoundedCornerShape(12.dp),
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
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isExpense) WarningRed else AccentGreen
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Save Transaction", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SummaryItem(title: String, amount: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, color = SurfaceWhite.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
            Text(amount, style = MaterialTheme.typography.titleMedium, color = SurfaceWhite, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun TransactionCard(expense: ExpenseEntity, currency: String) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(expense.date))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = BackgroundGray
            ) {
                Box(contentAlignment = Alignment.Center) {
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
                        modifier = Modifier.size(24.dp),
                        tint = PrimaryBlue
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.category, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(dateStr, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Text(
                text = "-$currency${String.format("%.0f", expense.amount)}",
                style = MaterialTheme.typography.titleLarge,
                color = WarningRed,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
