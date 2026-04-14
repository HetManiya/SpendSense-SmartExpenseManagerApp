package com.spendsense.app.frontend.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spendsense.app.frontend.components.PrimaryButton
import com.spendsense.app.frontend.components.StandardCard
import com.spendsense.app.frontend.theme.*
import com.spendsense.app.frontend.viewmodels.MainViewModel

@Composable
fun SettingsScreen(navController: NavController, viewModel: MainViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val dashboardState by viewModel.dashboardState.collectAsState()
    val suggestedBudget by viewModel.suggestedBudget.collectAsState()
    val groupId by viewModel.groupId.collectAsState()

    SettingsContent(
        initialName = userProfile?.name ?: "",
        initialCurrency = userProfile?.currencySymbol ?: "₹",
        initialBudget = dashboardState.budgetLimit,
        initialIncomeRange = userProfile?.incomeRange ?: "Medium",
        suggestedBudget = suggestedBudget,
        groupId = groupId,
        onSave = { name, currency, budget, range ->
            viewModel.saveUserProfile(name, currency, budget, range)
        },
        onLogout = {
            viewModel.logout()
        },
        onJoinGroup = { id ->
            viewModel.joinGroup(id)
        },
        onBack = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    initialName: String,
    initialCurrency: String,
    initialBudget: Double,
    initialIncomeRange: String,
    suggestedBudget: Double,
    groupId: String?,
    onSave: (String, String, Double, String) -> Unit,
    onLogout: () -> Unit,
    onJoinGroup: (String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var currency by remember(initialCurrency) { mutableStateOf(initialCurrency) }
    var budget by remember(initialBudget) { mutableStateOf(initialBudget.toInt().toString()) }
    var incomeRange by remember(initialIncomeRange) { mutableStateOf(initialIncomeRange) }
    
    val ranges = listOf("Low", "Medium", "High")

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
                        text = "App Settings",
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
                modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Collaborative Mode
                item {
                    StandardCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryBlue.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.CloudSync, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Cloud Collaboration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        if (groupId == null) {
                            var groupInput by remember { mutableStateOf("") }
                            Text("Sync your transactions and budgets with family members across devices.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = groupInput,
                                onValueChange = { groupInput = it },
                                label = { Text("Group ID") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                leadingIcon = { Icon(Icons.Rounded.Groups, contentDescription = null, tint = PrimaryBlue) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { onJoinGroup(groupInput) },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Text("Join Sync Group", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = AccentGreen.copy(alpha = 0.05f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text("ACTIVE CLOUD SESSION", style = MaterialTheme.typography.labelSmall, color = AccentGreen, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Group ID: $groupId", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Operator Profile
                item {
                    StandardCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Personalization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = PrimaryBlue) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = incomeRange,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Income Profile") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                leadingIcon = { Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = PrimaryBlue) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                ranges.forEach { range ->
                                    DropdownMenuItem(
                                        text = { Text(range, fontWeight = FontWeight.Bold) },
                                        onClick = { incomeRange = range; expanded = false }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = currency,
                            onValueChange = { currency = it },
                            label = { Text("Default Currency Symbol") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Rounded.CurrencyExchange, contentDescription = null, tint = PrimaryBlue) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
                        )
                    }
                }

                // Budget Limits
                item {
                    StandardCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Spending Thresholds", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        OutlinedTextField(
                            value = budget,
                            onValueChange = { if (it.all { c -> c.isDigit() }) budget = it },
                            label = { Text("Global Monthly Budget Cap") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Rounded.Timeline, contentDescription = null, tint = PrimaryBlue) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = PrimaryBlue.copy(alpha = 0.05f)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("AI SUGGESTED TARGET", style = MaterialTheme.typography.labelSmall, color = PrimaryBlue, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                    Text(
                                        text = "Optimize at: $currency${String.format("%.0f", suggestedBudget)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    PrimaryButton(
                        text = "Sync & Save Settings",
                        onClick = {
                            onSave(name, currency, budget.toDoubleOrNull() ?: 0.0, incomeRange)
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                    )
                }
                
                item {
                    TextButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp), tint = WarningRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Disconnect My Account", color = WarningRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}
