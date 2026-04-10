package com.spendsense.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.spendsense.app.ui.components.StandardCard
import com.spendsense.app.ui.theme.*
import com.spendsense.app.ui.viewmodels.MainViewModel

@Composable
fun SettingsScreen(navController: NavController, viewModel: MainViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val dashboardState by viewModel.dashboardState.collectAsState()
    val suggestedBudget by viewModel.suggestedBudget.collectAsState()
    val groupId by viewModel.groupId.collectAsState()

    SettingsContent(
        initialName = userProfile?.name ?: "",
        initialCurrency = userProfile?.currencySymbol ?: "$",
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
        }
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
    onJoinGroup: (String) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var currency by remember(initialCurrency) { mutableStateOf(initialCurrency) }
    var budget by remember(initialBudget) { mutableStateOf(initialBudget.toString()) }
    var incomeRange by remember(initialIncomeRange) { mutableStateOf(initialIncomeRange) }
    
    val ranges = listOf("Low", "Medium", "High")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Configuration",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // Family Mode Section
        item {
            StandardCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Family Mode (Sync)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                if (groupId == null) {
                    var groupInput by remember { mutableStateOf("") }
                    Text("Join a group to share transactions and budgets with family.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = groupInput,
                        onValueChange = { groupInput = it },
                        label = { Text("Group ID") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onJoinGroup(groupInput) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("JOIN CLOUD GROUP")
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AccentGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("CONNECTED TO GROUP", style = MaterialTheme.typography.labelSmall, color = AccentGreen, fontWeight = FontWeight.Bold)
                            Text("ID: $groupId", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // Profile Section
        item {
            StandardCard(modifier = Modifier.fillMaxWidth()) {
                Text("Operator Profile", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = incomeRange,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Income Range") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(SurfaceWhite)
                    ) {
                        ranges.forEach { range ->
                            DropdownMenuItem(
                                text = { Text(range) },
                                onClick = { incomeRange = range; expanded = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text("Currency Symbol") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Budget Section
        item {
            StandardCard(modifier = Modifier.fillMaxWidth()) {
                Text("Threshold Limits", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Monthly Budget Cap") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // AI Suggestion Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryBlue.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("AI ESTIMATE", style = MaterialTheme.typography.labelSmall, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                        Text(
                            text = "SUGGESTED: $currency${String.format("%.2f", suggestedBudget)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    onSave(name, currency, budget.toDoubleOrNull() ?: 0.0, incomeRange)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("SYNC SETTINGS", fontWeight = FontWeight.Bold)
            }
        }
        
        item {
            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("DISCONNECT ACCOUNT", color = WarningRed.copy(alpha = 0.7f))
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SpendSenseTheme {
        SettingsContent(
            initialName = "Operator-01",
            initialCurrency = "$",
            initialBudget = 1000.0,
            initialIncomeRange = "Medium",
            suggestedBudget = 1200.0,
            groupId = null,
            onSave = { _, _, _, _ -> },
            onLogout = {},
            onJoinGroup = {}
        )
    }
}
