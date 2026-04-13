package com.spendsense.app.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.spendsense.app.frontend.components.StandardCard
import com.spendsense.app.frontend.theme.*
import com.spendsense.app.frontend.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: MainViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val dashboardState by viewModel.dashboardState.collectAsState()
    val suggestedBudget by viewModel.suggestedBudget.collectAsState()
    val groupId by viewModel.groupId.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    // Navigate to Auth if logged out
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            navController.navigate(Screen.Auth.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    var name by remember(userProfile) { mutableStateOf(userProfile?.name ?: "") }
    var currency by remember(userProfile) { mutableStateOf(userProfile?.currencySymbol ?: "₹") }
    var budget by remember(dashboardState) { mutableStateOf(dashboardState.budgetLimit.toString()) }
    var incomeRange by remember(userProfile) { mutableStateOf(userProfile?.incomeRange ?: "Medium") }
    
    val ranges = listOf("Low", "Medium", "High")

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            // Account Info Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = PrimaryBlue.copy(alpha = 0.1f)
                    ) {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(20.dp),
                            tint = PrimaryBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = name.ifBlank { "User" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Managed by SpendSense AI",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Identity Section
            item {
                ProfileSectionTitle("Personal Identity")
                StandardCard(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
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
                            onDismissRequest = { expanded = false }
                        ) {
                            ranges.forEach { range ->
                                DropdownMenuItem(
                                    text = { Text(range) },
                                    onClick = { incomeRange = range; expanded = false }
                                )
                            }
                        }
                    }
                }
            }

            // Financial Preferences
            item {
                ProfileSectionTitle("Financial Configuration")
                StandardCard(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("Primary Currency Symbol") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = budget,
                        onValueChange = { budget = it },
                        label = { Text("Monthly Budget Cap") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryBlue.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Suggestion: $currency${String.format("%.0f", suggestedBudget)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Data & Sync Actions
            item {
                ProfileSectionTitle("Data & Collaboration")
                StandardCard(modifier = Modifier.fillMaxWidth()) {
                    // Export CSV Action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Export CSV Logic placeholder */ }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Description, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Export Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Download your data as CSV file", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = DividerGray)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerGray.copy(alpha = 0.5f))

                    // Group Sync Action
                    if (groupId == null) {
                        var showGroupInput by remember { mutableStateOf(false) }
                        if (!showGroupInput) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showGroupInput = true }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.GroupAdd, contentDescription = null, tint = PrimaryBlue)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Join Sync Group", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("Collaborate on budgets with family", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = DividerGray)
                                }
                                
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerGray.copy(alpha = 0.5f))
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.createGroup() }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Groups, contentDescription = null, tint = PrimaryBlue)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Create New Group", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("Start a new shared budget workspace", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = DividerGray)
                                }
                            }
                        } else {
                            var groupInput by remember { mutableStateOf("") }
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                OutlinedTextField(
                                    value = groupInput,
                                    onValueChange = { groupInput = it },
                                    label = { Text("Group ID") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { showGroupInput = false }, modifier = Modifier.weight(1f)) {
                                        Text("Cancel")
                                    }
                                    Button(
                                        onClick = { viewModel.joinGroup(groupInput) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Join Group")
                                    }
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AccentGreen.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.CloudDone, contentDescription = null, tint = AccentGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Active Sync: $groupId", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Your data is being shared with this group", style = MaterialTheme.typography.labelSmall, color = AccentGreen)
                            }
                            IconButton(onClick = { viewModel.leaveGroup() }) {
                                Icon(Icons.Rounded.Logout, contentDescription = "Leave Group", tint = WarningRed)
                            }
                        }
                    }
                }
            }

            // Actions
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.saveUserProfile(name, currency, budget.toDoubleOrNull() ?: 0.0, incomeRange)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out Account", color = WarningRed)
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun ProfileSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = TextSecondary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}
