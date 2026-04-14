package com.spendsense.app.frontend.screens

import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        suggestedBudget = suggestedBudget,
        onBack = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsContent(
    expenses: List<ExpenseEntity>,
    insights: List<String>,
    suggestedBudget: Double,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            Column(modifier = Modifier.statusBarsPadding().padding(horizontal = 24.dp, vertical = 12.dp)) {
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
                        text = "Smart Reports",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Box(modifier = Modifier.size(44.dp))
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(
            brush = Brush.verticalGradient(
                colors = listOf(BackgroundGray, SurfaceWhite)
            )
        )) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Suggested Budget - Enhanced Design
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(32.dp), spotColor = PrimaryBlue.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(32.dp),
                        color = PrimaryBlue
                    ) {
                        Column(modifier = Modifier.padding(28.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.TipsAndUpdates, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI SUGGESTED BUDGET", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "₹${String.format("%.0f", suggestedBudget)}",
                                style = MaterialTheme.typography.displayMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-1).sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Your ideal spending target based on recent history and velocity.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // AI Insights Section
                item {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryBlue.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Personal Insights", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        if (insights.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PrimaryBlue, strokeWidth = 3.dp)
                            }
                        } else {
                            insights.forEach { insight ->
                                InsightCardModern(text = insight)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }

                // Export Section
                item {
                    StandardCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.IosShare, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Export Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Download your transactions as a CSV spreadsheet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                label = { Text("From") },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("YYYY-MM-DD") },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                label = { Text("To") },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("YYYY-MM-DD") },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        PrimaryButton(
                            text = "Download CSV Report",
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
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun InsightCardModern(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
