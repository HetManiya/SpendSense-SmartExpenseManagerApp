package com.spendsense.app.frontend.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            Column(modifier = Modifier.statusBarsPadding().padding(horizontal = 24.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.clip(CircleShape).background(Color.White).size(44.dp)
                    ) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = "Profile & Settings",
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
                item { Spacer(modifier = Modifier.height(12.dp)) }

                // Avatar Section
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(110.dp).shadow(8.dp, CircleShape, spotColor = PrimaryBlue.copy(alpha = 0.4f)),
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = PrimaryBlue
                                )
                                // Decorative ring
                                Surface(
                                    modifier = Modifier.size(110.dp),
                                    shape = CircleShape,
                                    color = Color.Transparent,
                                    border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryBlue.copy(alpha = 0.1f))
                                ) {}
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = name.ifBlank { "SpendSense User" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryBlue.copy(alpha = 0.05f),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = "AI Premium Member",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // Identity Section
                item {
                    ProfileSectionTitleModern("Identity & Personalization")
                    StandardCard(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null, tint = PrimaryBlue) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = incomeRange,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Annual Income Range") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                leadingIcon = { Icon(Icons.Rounded.BarChart, contentDescription = null, tint = PrimaryBlue) },
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
                    }
                }

                // Financial Preferences
                item {
                    ProfileSectionTitleModern("Financial Controls")
                    StandardCard(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = currency,
                            onValueChange = { currency = it },
                            label = { Text("Currency Symbol") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Rounded.Payments, contentDescription = null, tint = PrimaryBlue) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedTextField(
                            value = budget,
                            onValueChange = { if (it.all { c -> c.isDigit() }) budget = it },
                            label = { Text("Monthly Budget Goal") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Rounded.TrackChanges, contentDescription = null, tint = PrimaryBlue) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = PrimaryBlue.copy(alpha = 0.05f)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("AI SUGGESTION", style = MaterialTheme.typography.labelSmall, color = PrimaryBlue, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                    Text(
                                        text = "Your ideal monthly cap: $currency${String.format("%.0f", suggestedBudget)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Sync & Groups
                item {
                    ProfileSectionTitleModern("Cloud & Collaboration")
                    StandardCard(modifier = Modifier.fillMaxWidth()) {
                        if (groupId == null) {
                            var showJoinInput by remember { mutableStateOf(false) }
                            
                            if (!showJoinInput) {
                                ProfileActionItem(
                                    title = "Join Sync Group",
                                    subtitle = "Connect to shared family budget",
                                    icon = Icons.Rounded.GroupAdd,
                                    onClick = { showJoinInput = true }
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerGray.copy(alpha = 0.3f))
                                ProfileActionItem(
                                    title = "Create New Group",
                                    subtitle = "Host a new collaborative space",
                                    icon = Icons.Rounded.Groups,
                                    onClick = { viewModel.createGroup() }
                                )
                            } else {
                                var groupInput by remember { mutableStateOf("") }
                                Column {
                                    OutlinedTextField(
                                        value = groupInput,
                                        onValueChange = { groupInput = it },
                                        label = { Text("Invite Group ID") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        TextButton(onClick = { showJoinInput = false }, modifier = Modifier.weight(1f)) {
                                            Text("Cancel")
                                        }
                                        Button(
                                            onClick = { viewModel.joinGroup(groupInput) },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                        ) {
                                            Text("Connect")
                                        }
                                    }
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = AccentGreen.copy(alpha = 0.05f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen.copy(alpha = 0.1f))
                            ) {
                                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.CloudDone, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Active Sync", style = MaterialTheme.typography.labelSmall, color = AccentGreen, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                        Text(groupId!!, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                                    }
                                    IconButton(
                                        onClick = { viewModel.leaveGroup() },
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(WarningRed.copy(alpha = 0.1f))
                                    ) {
                                        Icon(Icons.Rounded.Logout, contentDescription = "Leave", tint = WarningRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Danger Zone
                item {
                    Button(
                        onClick = {
                            viewModel.saveUserProfile(name, currency, budget.toDoubleOrNull() ?: 0.0, incomeRange)
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp).shadow(8.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Save Profile Changes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TextButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ExitToApp, contentDescription = null, tint = WarningRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Log Out Securely", color = WarningRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun ProfileActionItem(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = BackgroundGray) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = DividerGray)
    }
}

@Composable
fun ProfileSectionTitleModern(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = TextSecondary,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}
