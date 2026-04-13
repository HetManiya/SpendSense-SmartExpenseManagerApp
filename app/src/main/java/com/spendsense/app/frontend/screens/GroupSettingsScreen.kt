package com.spendsense.app.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spendsense.app.frontend.components.PrimaryButton
import com.spendsense.app.frontend.components.SecondaryButton
import com.spendsense.app.frontend.components.StandardCard
import com.spendsense.app.frontend.theme.*
import com.spendsense.app.frontend.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSettingsScreen(navController: NavController, viewModel: MainViewModel) {
    val groupId by viewModel.groupId.collectAsState()
    var joinId by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (groupId == null) {
                // No Group View
                Icon(
                    imageVector = Icons.Rounded.Groups,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = PrimaryBlue.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Sync with Family or Friends",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Create a group to share expenses and budgets in real-time.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                StandardCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Join an Existing Group", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = joinId,
                        onValueChange = { joinId = it.uppercase() },
                        label = { Text("Enter Group ID") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(
                        text = "Join Group",
                        onClick = { 
                            if (joinId.isNotEmpty()) {
                                viewModel.joinGroup(joinId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = joinId.length >= 4
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("OR", color = TextSecondary, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                SecondaryButton(
                    text = "Create New Group",
                    onClick = { viewModel.createGroup() },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Active Group View
                StandardCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = PrimaryBlue
                ) {
                    Text("Active Group Sync", color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = groupId ?: "",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Share this ID with others to let them join your group.",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Group Features Enabled:",
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                FeatureRow(Icons.Rounded.CloudSync, "Real-time Expense Sync")
                FeatureRow(Icons.Rounded.PieChart, "Shared Monthly Budget")
                FeatureRow(Icons.Rounded.Security, "Secure End-to-End Encryption")
                
                Spacer(modifier = Modifier.weight(1f))
                
                TextButton(
                    onClick = { viewModel.leaveGroup() },
                    colors = ButtonDefaults.textButtonColors(contentColor = WarningRed)
                ) {
                    Text("Leave Group", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
