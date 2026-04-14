package com.spendsense.app.frontend.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
                        text = "Cloud Groups",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Box(modifier = Modifier.size(44.dp))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundGray
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(
            brush = Brush.verticalGradient(
                colors = listOf(BackgroundGray, SurfaceWhite)
            )
        )) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (groupId == null) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Surface(
                        modifier = Modifier.size(120.dp).shadow(12.dp, CircleShape, spotColor = PrimaryBlue.copy(alpha = 0.4f)),
                        shape = CircleShape,
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Groups,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = PrimaryBlue
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = "Collaborative Spending",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-0.5).sp
                    )
                    
                    Text(
                        text = "Sync budgets and expenses with your family or partner in real-time.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    StandardCard(modifier = Modifier.fillMaxWidth()) {
                        Text("JOIN A GROUP", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedTextField(
                            value = joinId,
                            onValueChange = { joinId = it.uppercase() },
                            label = { Text("Invite Group ID") },
                            placeholder = { Text("e.g. ABC-123") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Rounded.Numbers, contentDescription = null, tint = PrimaryBlue) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        PrimaryButton(
                            text = "Connect to Group",
                            onClick = { 
                                if (joinId.isNotEmpty()) {
                                    viewModel.joinGroup(joinId)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = joinId.length >= 4
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(DividerGray.copy(alpha = 0.5f)))
                        Text("OR", color = TextSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(DividerGray.copy(alpha = 0.5f)))
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Surface(
                        onClick = { viewModel.createGroup() },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = PrimaryBlue.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.1f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.AddCircle, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Create New Group Workspace", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(32.dp), spotColor = PrimaryBlue.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(32.dp),
                        color = PrimaryBlue
                    ) {
                        Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("YOUR CLOUD WORKSPACE", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = groupId ?: "",
                                style = MaterialTheme.typography.displayMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 3.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Share this ID with others to collaborate on this budget.",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Active Group Syncing", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        FeatureRowModern(Icons.Rounded.CloudSync, "Real-time Expense Syncing", "Every transaction is shared instantly.")
                        FeatureRowModern(Icons.Rounded.InsertChart, "Unified Analytics", "View combined spending reports.")
                        FeatureRowModern(Icons.Rounded.VerifiedUser, "End-to-End Encryption", "Your financial data remains private.")
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    TextButton(
                        onClick = { viewModel.leaveGroup() },
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ExitToApp, contentDescription = null, tint = WarningRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Disconnect from Group", color = WarningRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureRowModern(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(16.dp),
            color = PrimaryBlue.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}
