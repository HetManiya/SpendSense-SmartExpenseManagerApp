package com.spendsense.app.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import com.spendsense.app.frontend.theme.*
import com.spendsense.app.frontend.viewmodels.MainViewModel

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
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Transaction History",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search transactions...", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = DividerGray,
                        cursorColor = PrimaryBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL", "EXPENSE", "INCOME").forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { onTypeChange(type) },
                            label = { Text(type, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue.copy(alpha = 0.1f),
                                selectedLabelColor = PrimaryBlue,
                                labelColor = TextSecondary
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(transactions) { transaction ->
                TransactionCard(transaction, currency, onDelete, onEdit)
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: Transaction,
    currency: String,
    onDelete: (Transaction) -> Unit,
    onEdit: (Transaction) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = SurfaceWhite,
            title = { Text("Delete Transaction?", color = WarningRed, style = MaterialTheme.typography.titleLarge) },
            text = { Text("Are you sure you want to delete this record? This action cannot be undone.", color = TextPrimary) },
            confirmButton = {
                TextButton(onClick = { onDelete(transaction); showDeleteDialog = false }) {
                    Text("Delete", color = WarningRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(transaction) },
        shape = RoundedCornerShape(24.dp),
        color = SurfaceWhite
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isExpense = transaction is Transaction.Expense
            val tintColor = if (isExpense) WarningRed else AccentGreen
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tintColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpense) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                if (transaction.note.isNotBlank()) {
                    Text(transaction.note, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val prefix = if (isExpense) "-" else "+"
                Text(
                    text = "$prefix$currency${String.format("%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = tintColor,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    if (isExpense) {
                        IconButton(onClick = { onEdit(transaction) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    SpendSenseTheme {
        HistoryContent(
            transactions = listOf(
                Transaction.Expense(ExpenseEntity(1, 150.0, "Shopping", System.currentTimeMillis(), "New Boots", "Card")),
                Transaction.Income(IncomeEntity(2, 5000.0, System.currentTimeMillis(), "Salary", "Monthly Pay"))
            ),
            currency = "₹",
            searchQuery = "",
            selectedType = "ALL",
            onSearchChange = {},
            onTypeChange = {},
            onDelete = {},
            onEdit = {}
        )
    }
}
