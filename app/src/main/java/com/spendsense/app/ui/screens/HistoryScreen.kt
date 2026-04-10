package com.spendsense.app.ui.screens

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
import com.spendsense.app.data.local.ExpenseEntity
import com.spendsense.app.ui.theme.*
import com.spendsense.app.ui.viewmodels.MainViewModel

@Composable
fun HistoryScreen(navController: NavController, viewModel: MainViewModel) {
    val expenses by viewModel.allExpenses.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("ALL") }

    val filteredExpenses = expenses.filter {
        val matchesSearch = it.category.contains(searchQuery, ignoreCase = true) || it.note.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedType == "ALL" || (selectedType == "EXPENSE")
        matchesSearch && matchesType
    }

    HistoryContent(
        expenses = filteredExpenses,
        searchQuery = searchQuery,
        selectedType = selectedType,
        onSearchChange = { searchQuery = it },
        onTypeChange = { selectedType = it },
        onDelete = { viewModel.deleteExpense(it) },
        onEdit = { expense ->
            navController.navigate(Screen.AddEntry.createRoute(expense.id))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    expenses: List<ExpenseEntity>,
    searchQuery: String,
    selectedType: String,
    onSearchChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onDelete: (ExpenseEntity) -> Unit,
    onEdit: (ExpenseEntity) -> Unit
) {
    Scaffold(
        containerColor = DeepSlate,
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TRANSACTION LOG",
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search system logs...", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricBlue) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = GlassWhite,
                        cursorColor = ElectricBlue,
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
                                selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                                selectedLabelColor = ElectricBlue,
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
            items(expenses) { expense ->
                TransactionCard(expense, onDelete, onEdit)
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TransactionCard(
    expense: ExpenseEntity, 
    onDelete: (ExpenseEntity) -> Unit,
    onEdit: (ExpenseEntity) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = CardBackground,
            title = { Text("PURGE TRANSACTION?", color = WarningRed, style = MaterialTheme.typography.titleLarge) },
            text = { Text("Are you sure you want to delete this record? This action cannot be undone.", color = TextPrimary) },
            confirmButton = {
                TextButton(onClick = { onDelete(expense); showDeleteDialog = false }) {
                    Text("CONFIRM", color = WarningRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardBackground)
            .clickable { onEdit(expense) }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElectricBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(expense.category.take(1).uppercase(), color = ElectricBlue, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.category.uppercase(), style = MaterialTheme.typography.titleMedium, color = ElectricBlue)
                if (expense.note.isNotBlank()) {
                    Text(expense.note, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
                Text(expense.paymentMethod, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("-$${String.format("%.2f", expense.amount)}", style = MaterialTheme.typography.titleLarge, color = WarningRed)
                Row {
                    IconButton(onClick = { onEdit(expense) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ElectricBlue.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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
            expenses = listOf(
                ExpenseEntity(1, 150.0, "Shopping", System.currentTimeMillis(), "New Boots", "Card"),
                ExpenseEntity(2, 45.0, "Food", System.currentTimeMillis(), "Dinner", "Cash")
            ),
            searchQuery = "",
            selectedType = "ALL",
            onSearchChange = {},
            onTypeChange = {},
            onDelete = {},
            onEdit = {}
        )
    }
}
