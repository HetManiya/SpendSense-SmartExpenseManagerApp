package com.spendsense.app.frontend.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
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
import com.spendsense.app.backend.local.GoalEntity
import com.spendsense.app.frontend.components.CustomProgressBar
import com.spendsense.app.frontend.components.GlassCard
import com.spendsense.app.frontend.components.PrimaryButton
import com.spendsense.app.frontend.components.StandardCard
import com.spendsense.app.frontend.theme.*
import com.spendsense.app.frontend.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GoalsScreen(navController: NavController, viewModel: MainViewModel) {
    val goals by viewModel.allGoals.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    GoalsContent(
        goals = goals,
        onAddClick = { showAddDialog = true },
        onDelete = { viewModel.deleteGoal(it) },
        onContribute = { goal, amount -> viewModel.updateGoalProgress(goal, amount) },
        onBack = { navController.popBackStack() }
    )

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, target, deadline ->
                viewModel.addGoal(title, target, deadline)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsContent(
    goals: List<GoalEntity>,
    onAddClick: () -> Unit,
    onDelete: (GoalEntity) -> Unit,
    onContribute: (GoalEntity, Double) -> Unit,
    onBack: () -> Unit
) {
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
                        text = "Savings Goals",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(
                        onClick = onAddClick,
                        modifier = Modifier.clip(CircleShape).background(PrimaryBlue).size(44.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Goal", tint = Color.White)
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
            if (goals.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = PrimaryBlue.copy(alpha = 0.05f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Flag, 
                                contentDescription = null, 
                                modifier = Modifier.size(56.dp), 
                                tint = PrimaryBlue.copy(alpha = 0.3f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("No goals set yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Start planning your future today!", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(32.dp))
                    PrimaryButton(text = "Create First Goal", onClick = onAddClick, modifier = Modifier.padding(horizontal = 48.dp))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding).padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(goals) { goal ->
                        GoalCard(goal, onDelete, onContribute)
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }
}

@Composable
fun GoalCard(
    goal: GoalEntity,
    onDelete: (GoalEntity) -> Unit,
    onContribute: (GoalEntity, Double) -> Unit
) {
    var showContributeDialog by remember { mutableStateOf(false) }
    val progress = if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount).toFloat() else 0f
    val isCompleted = progress >= 1f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(28.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(28.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isCompleted) AccentGreen.copy(alpha = 0.1f) else PrimaryBlue.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Rounded.Celebration else Icons.Rounded.Flag, 
                            contentDescription = null, 
                            tint = if (isCompleted) AccentGreen else PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title, 
                        style = MaterialTheme.typography.titleLarge, 
                        color = TextPrimary, 
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Target: ₹${String.format("%.0f", goal.targetAmount)}", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = { onDelete(goal) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = WarningRed.copy(alpha = 0.4f))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("SAVED", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(
                        text = "₹${String.format("%.0f", goal.savedAmount)}", 
                        style = MaterialTheme.typography.headlineSmall, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = if (isCompleted) AccentGreen else TextPrimary
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}%", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = if (isCompleted) AccentGreen else PrimaryBlue
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            CustomProgressBar(
                progress = progress, 
                color = if (isCompleted) AccentGreen else PrimaryBlue,
                modifier = Modifier.height(10.dp)
            )
            
            if (!isCompleted) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showContributeDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Contribution", fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AccentGreen.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Goal Achieved!", color = AccentGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    if (showContributeDialog) {
        var amount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showContributeDialog = false },
            containerColor = Color.White,
            title = { Text("Contribute Funds", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column {
                    Text("Adding funds to: ${goal.title}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                        label = { Text("Amount") },
                        prefix = { Text("₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) onContribute(goal, amt)
                        showContributeDialog = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Contribute")
                }
            },
            dismissButton = {
                TextButton(onClick = { showContributeDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onConfirm: (String, Double, Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Set New Goal", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What are you saving for?") },
                    placeholder = { Text("e.g. New Car, Vacation") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextSecondary) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = target,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) target = it },
                    label = { Text("Target Amount") },
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Rounded.Payments, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextSecondary) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val t = target.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && t > 0) {
                        onConfirm(title, t, System.currentTimeMillis() + 2592000000L) // Default 30 days
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Create Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
