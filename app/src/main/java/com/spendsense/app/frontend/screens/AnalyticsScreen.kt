package com.spendsense.app.frontend.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spendsense.app.backend.local.ExpenseEntity
import com.spendsense.app.frontend.components.GlassCard
import com.spendsense.app.frontend.components.StandardCard
import com.spendsense.app.frontend.theme.*
import com.spendsense.app.frontend.viewmodels.MainViewModel
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
        onEndDateChange = { endDate = it },
        onBack = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    onEndDateChange: (String) -> Unit,
    onBack: () -> Unit
) {
    var showFilters by remember { mutableStateOf(false) }
    val expenseCategories = listOf("All", "Food", "Travel", "Shopping", "Auto", "Rent", "Health", "Entertainment", "Others")

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
                        text = "Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Surface(
                        onClick = { showFilters = !showFilters },
                        shape = CircleShape,
                        color = if (showFilters) PrimaryBlue else Color.White,
                        modifier = Modifier.size(44.dp),
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (showFilters) Icons.Rounded.FilterListOff else Icons.Rounded.FilterList, 
                                contentDescription = "Filter", 
                                tint = if (showFilters) Color.White else PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
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
                modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    AnimatedVisibility(
                        visible = showFilters,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        StandardCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Apply Filters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            CategorySelectorModern("Category", categoryFilter, expenseCategories, onCategoryChange)
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Amount Range", style = MaterialTheme.typography.labelMedium, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Text("₹${amountRange.start.toInt()} - ₹${amountRange.endInclusive.toInt()}", style = MaterialTheme.typography.labelMedium, color = PrimaryBlue, fontWeight = FontWeight.ExtraBold)
                            }
                            RangeSlider(
                                value = amountRange,
                                onValueChange = onAmountRangeChange,
                                valueRange = 0f..20000f,
                                colors = SliderDefaults.colors(
                                    thumbColor = PrimaryBlue,
                                    activeTrackColor = PrimaryBlue,
                                    inactiveTrackColor = DividerGray
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DateInputModern("From", startDate, onStartDateChange, Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(12.dp))
                                DateInputModern("To", endDate, onEndDateChange, Modifier.weight(1f))
                            }
                        }
                    }
                }

                val categoryGroups = expenses.groupBy { it.category }
                    .mapValues { it.value.sumOf { exp -> exp.amount } }
                    .toList().sortedByDescending { it.second }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(32.dp),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(28.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Spending Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                                Icon(Icons.Rounded.DonutLarge, contentDescription = null, tint = PrimaryBlue.copy(alpha = 0.4f))
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            if (categoryGroups.isEmpty()) {
                                EmptyAnalyticsStateModern()
                            } else {
                                var selectedIndex by remember { mutableStateOf(-1) }
                                val animationProgress = remember { Animatable(0f) }
                                
                                LaunchedEffect(categoryGroups) {
                                    animationProgress.snapTo(0f)
                                    animationProgress.animateTo(1f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
                                }

                                Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                                    Canvas(modifier = Modifier.size(200.dp)) {
                                        var startAngle = -90f
                                        val total = categoryGroups.sumOf { it.second }.toFloat()
                                        val colors = listOf(PrimaryBlue, AccentGreen, WarningRed, Color(0xFFFFB300), Color(0xFF9C27B0), Color(0xFF00BCD4))
                                        
                                        categoryGroups.forEachIndexed { index, pair ->
                                            val sweepAngle = (pair.second.toFloat() / total) * 360f * animationProgress.value
                                            val isSelected = selectedIndex == index
                                            
                                            drawArc(
                                                color = colors[index % colors.size],
                                                startAngle = startAngle,
                                                sweepAngle = sweepAngle,
                                                useCenter = false,
                                                style = Stroke(width = if (isSelected) 50f else 35f, cap = StrokeCap.Round)
                                            )
                                            startAngle += sweepAngle
                                        }
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        AnimatedContent(targetState = selectedIndex, label = "chart_center") { idx ->
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                if (idx != -1 && idx < categoryGroups.size) {
                                                    Text(categoryGroups[idx].first.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                                    Text("₹${String.format("%.0f", categoryGroups[idx].second)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                                                } else {
                                                    Text("TOTAL SPENT", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                                    Text("₹${String.format("%.0f", expenses.sumOf { it.amount })}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    maxItemsInEachRow = 3
                                ) {
                                    val colors = listOf(PrimaryBlue, AccentGreen, WarningRed, Color(0xFFFFB300), Color(0xFF9C27B0))
                                    categoryGroups.forEachIndexed { index, pair ->
                                        LegendItemModern(pair.first, colors[index % colors.size], selectedIndex == index) {
                                            selectedIndex = if (selectedIndex == index) -1 else index
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (categoryGroups.isNotEmpty()) {
                    item {
                        Text("Category Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    }
                    
                    items(categoryGroups) { (cat, amt) ->
                        CategoryMetricRowModern(cat, amt, expenses.sumOf { it.amount })
                    }
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = AccentGreen.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("AI PREDICTION", style = MaterialTheme.typography.labelSmall, color = AccentGreen, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                Text(
                                    text = "Based on your current pace, you'll likely spend ₹${String.format("%.0f", expenses.sumOf { it.amount } * 1.1)} by the end of the month.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun DateInputModern(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        placeholder = { Text("YYYY-MM-DD") },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = DividerGray
        )
    )
}

@Composable
fun LegendItemModern(label: String, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = if (isSelected) color else TextPrimary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
fun CategoryMetricRowModern(label: String, value: Double, total: Double) {
    val percentage = if (total > 0) (value / total).toFloat() else 0f
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = BackgroundGray) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Category, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryBlue)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Text("₹${String.format("%.0f", value)}", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = percentage,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = PrimaryBlue,
                trackColor = DividerGray.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("${(percentage * 100).toInt()}% of total", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
fun EmptyAnalyticsStateModern() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.BarChart, contentDescription = null, modifier = Modifier.size(64.dp).alpha(0.2f), tint = TextSecondary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Insufficient Data", style = MaterialTheme.typography.titleMedium, color = TextSecondary, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectorModern(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Rounded.Category, contentDescription = null, tint = PrimaryBlue) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = DividerGray
            )
        )
        ExposedDropdownMenu(
            expanded = expanded, 
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { sel ->
                DropdownMenuItem(
                    text = { Text(sel, fontWeight = FontWeight.Bold) }, 
                    onClick = { onSelect(sel); expanded = false }
                )
            }
        }
    }
}
