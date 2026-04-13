package com.spendsense.app.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        }
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
    onEdit: (Transaction) -> Unit
) {
    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            Column(modifier = Modifier.statusBarsPadding().padding(24.dp)) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search history...", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = DividerGray,
                        cursorColor = PrimaryBlue
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL", "EXPENSE", "INCOME").forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { onTypeChange(type) },
                            label = { Text(type, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedType == type,
                                borderColor = if (selectedType == type) Color.Transparent else DividerGray
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
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
                        modifier = Modifier.fillMaxWidth().padding(top = 100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = DividerGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No matching records", color = TextSecondary)
                    }
                }
            } else {
                items(transactions) { transaction ->
                    TransactionItem(transaction, currency, onDelete, onEdit)
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
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
            containerColor = SurfaceWhite,
            title = { Text("Delete Entry?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove this record from your history.") },
            confirmButton = {
                TextButton(onClick = { onDelete(transaction); showDeleteDialog = false }) {
                    Text("Delete", color = WarningRed, fontWeight = FontWeight.Bold)
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

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(transaction) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tintColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpense) Icons.Rounded.NorthEast else Icons.Rounded.SouthWest,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title, 
                    style = MaterialTheme.typography.titleMedium, 
                    color = TextPrimary, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(transaction.date)), 
                    style = MaterialTheme.typography.labelSmall, 
                    color = TextSecondary
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isExpense) "-" else "+") + currency + String.format("%.2f", transaction.amount),
                    style = MaterialTheme.typography.titleLarge,
                    color = tintColor,
                    fontWeight = FontWeight.ExtraBold
                )
                
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
