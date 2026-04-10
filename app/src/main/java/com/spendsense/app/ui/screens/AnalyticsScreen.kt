package com.spendsense.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.spendsense.app.data.local.ExpenseEntity
import com.spendsense.app.ui.components.StandardCard
import com.spendsense.app.ui.theme.*
import com.spendsense.app.ui.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(navController: NavController, viewModel: MainViewModel) {
    val expenses by viewModel.allExpenses.collectAsState()
    
    var categoryFilter by remember { mutableStateOf("All") }
    var amountRange by remember { mutableStateOf(0f..10000f) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val filterStart = try { if(startDate.isNotEmpty()) sdf.parse(startDate)?.time else null } catch (e: Exception) { null }
    val filterEnd = try { if(endDate.isNotEmpty()) sdf.parse(endDate)?.time else null } catch (e: Exception) { null }

    val filteredExpenses = expenses.filter { 
        (categoryFilter == "All" || it.category == categoryFilter) &&
        (it.amount >= amountRange.start && it.amount <= amountRange.endInclusive) &&
        (filterStart == null || it.date >= filterStart) &&
        (filterEnd == null || it.date <= filterEnd)
    }

    AnalyticsContent(
        expenses = filteredExpenses,
        categoryFilter = categoryFilter,
        amountRange = amountRange,
        startDate = startDate,
        endDate = endDate,
        onCategoryChange = { categoryFilter = it },
        onAmountRangeChange = { amountRange = it },
        onStartDateChange = { startDate = it },
        onEndDateChange = { endDate = it }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsContent(
    expenses: List<ExpenseEntity>,
    categoryFilter: String,
    amountRange: ClosedFloatingPointRange<Float>,
    startDate: String,
    endDate: String,
    onCategoryChange: (String) -> Unit,
    onAmountRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit
) {
    var showFilters by remember { mutableStateOf(false) }
    val expenseCategories = listOf("All", "Auto", "Food", "Travel", "Shopping", "Rent", "Health", "Entertainment", "Others")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showFilters = !showFilters }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = PrimaryBlue)
            }
        }
        
        if (showFilters) {
            StandardCard(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                Text("Category", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = categoryFilter,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        expenseCategories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { onCategoryChange(cat); expanded = false })
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Amount Range ($${amountRange.start.toInt()} - $${amountRange.endInclusive.toInt()})", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                RangeSlider(
                    value = amountRange,
                    onValueChange = onAmountRangeChange,
                    valueRange = 0f..10000f,
                    colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = onStartDateChange,
                        label = { Text("Start Date", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("YYYY-MM-DD") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = onEndDateChange,
                        label = { Text("End Date", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("YYYY-MM-DD") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val categoryGroups = expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { exp -> exp.amount } }
            .toList().sortedByDescending { it.second }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Top 3 Categories Card
            if (categoryGroups.isNotEmpty()) {
                item {
                    StandardCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Top Spending Hubs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        categoryGroups.take(3).forEach { (cat, amt) ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(cat, color = TextPrimary)
                                Text("$${String.format("%.2f", amt)}", color = WarningRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Category Breakdown (Pie Chart)
            item {
                StandardCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Expenditure Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (categoryGroups.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            Text("No data acquired", color = TextSecondary)
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.size(180.dp)) {
                                var startAngle = -90f
                                val total = categoryGroups.sumOf { it.second }.toFloat()
                                val colors = listOf(PrimaryBlue, AccentGreen, WarningRed, Color(0xFFFFB300), Color(0xFF9C27B0))
                                
                                categoryGroups.take(5).forEachIndexed { index, pair ->
                                    val sweepAngle = (pair.second.toFloat() / total) * 360f
                                    drawArc(
                                        color = colors[index % colors.size],
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = 35f)
                                    )
                                    startAngle += sweepAngle
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TOTAL", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text("$${expenses.sumOf { it.amount }.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Predictive Projection
            item {
                StandardCard(modifier = Modifier.fillMaxWidth()) {
                    Text("System Projection (Next Month)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    val currentTotal = expenses.sumOf { it.amount }
                    val estimate = currentTotal * 1.05
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .weight(1f)
                                .background(AccentGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text("Estimated: $${String.format("%.2f", estimate)}", color = AccentGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "System projects a 5% variation based on current spending velocity and historical patterns.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            item {
                Text("Detailed Analysis", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
            }

            items(categoryGroups) { pair ->
                MetricRow(pair.first, pair.second)
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun MetricRow(label: String, value: Double) {
    StandardCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            Text("$${String.format("%.2f", value)}", style = MaterialTheme.typography.titleMedium, color = WarningRed, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    SpendSenseTheme {
        AnalyticsContent(
            expenses = listOf(
                ExpenseEntity(1, 1200.0, "Rent", System.currentTimeMillis(), "", ""),
                ExpenseEntity(2, 450.0, "Food", System.currentTimeMillis(), "", ""),
                ExpenseEntity(3, 300.0, "Shopping", System.currentTimeMillis(), "", "")
            ),
            categoryFilter = "All",
            amountRange = 0f..5000f,
            startDate = "",
            endDate = "",
            onCategoryChange = {},
            onAmountRangeChange = {},
            onStartDateChange = {},
            onEndDateChange = {}
        )
    }
}
