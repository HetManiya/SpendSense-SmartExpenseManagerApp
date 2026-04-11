package com.spendsense.app.frontend.screens

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.spendsense.app.backend.local.ExpenseEntity
import com.spendsense.app.frontend.components.StandardCard
import com.spendsense.app.frontend.components.PrimaryButton
import com.spendsense.app.frontend.theme.*
import com.spendsense.app.frontend.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(navController: NavController, viewModel: MainViewModel) {
    val expenses by viewModel.allExpenses.collectAsState()
    val insights by viewModel.aiInsights.collectAsState()
    val suggestedBudget by viewModel.suggestedBudget.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.generateAiInsights()
    }

    ReportsContent(
        expenses = expenses,
        insights = insights,
        suggestedBudget = suggestedBudget
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsContent(
    expenses: List<ExpenseEntity>,
    insights: List<String>,
    suggestedBudget: Double
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BackgroundGray
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "Financial Reports",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Suggested Budget
            item {
                StandardCard(modifier = Modifier.fillMaxWidth()) {
                    Text("AI BUDGET TARGET", style = MaterialTheme.typography.labelSmall, color = PrimaryBlue)
                    Text(
                        text = "Suggested: ₹${String.format("%.2f", suggestedBudget)}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Based on your 30-day historical analysis and spending velocity.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // AI Insights Section
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Insights", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (insights.isEmpty()) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    } else {
                        insights.forEach { insight ->
                            InsightCard(text = insight)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            // Filtering & Export Section
            item {
                StandardCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Export Data", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Filter and export your financial data as a CSV file.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Start Date", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("YYYY-MM-DD") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = DividerGray
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("YYYY-MM-DD") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = DividerGray
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    PrimaryButton(
                        text = "Generate CSV Report",
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                    val file = File(downloadsDir, "SpendSense_Report_${System.currentTimeMillis()}.csv")
                                    val writer = FileWriter(file)
                                    writer.append("Date,Category,Amount,Note,PaymentMethod\n")
                                    
                                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val filterStart = try { sdf.parse(startDate)?.time } catch (e: Exception) { null }
                                    val filterEnd = try { sdf.parse(endDate)?.time } catch (e: Exception) { null }

                                    expenses.filter { exp ->
                                        val matchesStart = filterStart == null || exp.date >= filterStart
                                        val matchesEnd = filterEnd == null || exp.date <= filterEnd
                                        matchesStart && matchesEnd
                                    }.forEach { exp ->
                                        val dateStr = sdf.format(Date(exp.date))
                                        writer.append("$dateStr,${exp.category},${exp.amount},${exp.note},${exp.paymentMethod}\n")
                                    }
                                    writer.flush()
                                    writer.close()
                                    Toast.makeText(context, "Report exported to Downloads", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun InsightCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = PrimaryVariant
    ) {
        PaddingValues(16.dp).let {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportsScreenPreview() {
    SpendSenseTheme {
        ReportsContent(
            expenses = emptyList(),
            insights = listOf(
                "Spending in 'Food' is 15% higher than last month.",
                "Switch to 'Cash' for minor transactions.",
                "40% of expenses occur on weekends."
            ),
            suggestedBudget = 1450.0
        )
    }
}
