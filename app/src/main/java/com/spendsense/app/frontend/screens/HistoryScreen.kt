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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spendsense.app.backend.local.ExpenseEntity
import com.spendsense.app.backend.local.IncomeEntity
import com.spendsense.app.frontend.components.GlassCard
import com.spendsense.app.frontend.theme.*
import com.spendsense.app.frontend.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

sealed class Transaction {
    abstract val id: Int
    abstract val amount: Double
    abstract val date: Long
    abstract val note: String
    abstract val title: String

    data class Expense(val entity: ExpenseEntity) : Transaction() {
        override val id = entity.id
        override val amount = entity.amount
        override val date = entity.date
        override val note = entity.note
        override val title = entity.category
    }

    data class Income(val entity: IncomeEntity) : Transaction() {
        override val id = entity.id
        override val amount = entity.amount
        override val date = entity.date
        override val note = entity.note
        override val title = entity.source
    }
}

@Composable
fun HistoryScreen(navController: NavController, viewModel: MainViewModel) {
    val expenses by viewModel.allExpenses.collectAsState()
    val incomes by viewModel.allIncomes.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val currency = userProfile?.currencySymbol ?: "₹"
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("ALL") }

    val transactions = remember(expenses, incomes, searchQuery, selectedType) {
        val combined = mutableListOf<Transaction>()
        if (selectedType == "ALL" || selectedType == "EXPENSE") {
            combined.addAll(expenses.map { Transaction.Expense(it) })
        }
        if (selectedType == "ALL" || selectedType == "INCOME") {
            combined.addAll(incomes.map { Transaction.Income(it) })
        }
        
        combined.filter {
            it.title.contains(searchQuery, ignoreCase = true) || it.note.contains(searchQuery, ignoreCase = true)
        }.sortedByDescending { it.date }
    }

    HistoryContent(
        transactions = transactions,
        currency = currency,
        searchQuery = searchQuery,
        selectedType = selectedType,
        onSearchChange = { searchQuery = it },
        onTypeChange = { selectedType = it },
        onDelete = { transaction ->
            when (transaction) {
                is Transaction.Expense -> viewModel.deleteExpense(transaction.entity)
                is Transaction.Income -> viewModel.deleteIncome(transaction.entity)
            }
        },
        onEdit = { transaction ->
            if (transaction is Transaction.Expense) {
                navController.navigate(Screen.AddEntry.createRoute(transaction.id))
            }
        },
        onBack = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    transactions: List<Transaction>,
    currency: String,
    searchQuery: String,
    selectedType: String,
    onSearchChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onDelete: (Transaction) -> Unit,
    onEdit: (Transaction) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .background(Color.Transparent)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.clip(CircleShape).background(Color.White).size(44.dp)
                    ) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Box(modifier = Modifier.size(44.dp)) // Spacer
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search transactions...", color = TextSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = PrimaryBlue) },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = PrimaryBlue
                    )
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("ALL", "EXPENSE", "INCOME").forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            onClick = { onTypeChange(type) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) PrimaryBlue else Color.White,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shadowElevation = if (isSelected) 4.dp else 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = type, 
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(
            brush = Brush.verticalGradient(
                colors = listOf(BackgroundGray, SurfaceWhite)
            )
        )) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (transactions.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                modifier = Modifier.size(100.dp),
                                shape = CircleShape,
                                color = BackgroundGray
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = DividerGray)
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("No records found", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Try adjusting your search or filters", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                } else {
                    val grouped = transactions.groupBy { 
                        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(it.date))
                    }
                    
                    grouped.forEach { (month, txs) ->
                        item {
                            Text(
                                text = month,
                                style = MaterialTheme.typography.labelLarge,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(txs) { transaction ->
                            TransactionItem(transaction, currency, onDelete, onEdit)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    currency: String,
    onDelete: (Transaction) -> Unit,
    onEdit: (Transaction) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isExpense = transaction is Transaction.Expense
    val tintColor = if (isExpense) WarningRed else AccentGreen

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            title = { Text("Delete Entry?", fontWeight = FontWeight.ExtraBold) },
            text = { Text("This will permanently remove this record from your history.") },
            confirmButton = {
                Button(
                    onClick = { onDelete(transaction); showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(transaction) }
            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = tintColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when(transaction.title) {
                            "Food" -> Icons.Rounded.Restaurant
                            "Shopping" -> Icons.Rounded.ShoppingCart
                            "Travel" -> Icons.Rounded.DirectionsBus
                            "Rent" -> Icons.Rounded.Home
                            "Health" -> Icons.Rounded.HealthAndSafety
                            "Salary" -> Icons.Rounded.AccountBalance
                            else -> if (isExpense) Icons.Rounded.Payments else Icons.Rounded.Savings
                        },
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title, 
                    style = MaterialTheme.typography.bodyLarge, 
                    color = TextPrimary, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("MMM dd • hh:mm a", Locale.getDefault()).format(Date(transaction.date)), 
                    style = MaterialTheme.typography.labelSmall, 
                    color = TextSecondary
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isExpense) "-" else "+") + currency + String.format("%.0f", transaction.amount),
                    style = MaterialTheme.typography.titleLarge,
                    color = tintColor,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                
                IconButton(
                    onClick = { showDeleteDialog = true }, 
                    modifier = Modifier.size(32.dp).padding(top = 4.dp)
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
