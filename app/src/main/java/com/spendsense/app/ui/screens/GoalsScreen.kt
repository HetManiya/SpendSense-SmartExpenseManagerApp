package com.spendsense.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.spendsense.app.data.local.GoalEntity
import com.spendsense.app.ui.components.CustomProgressBar
import com.spendsense.app.ui.components.PrimaryButton
import com.spendsense.app.ui.components.StandardCard
import com.spendsense.app.ui.theme.*
import com.spendsense.app.ui.viewmodels.MainViewModel
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
        onContribute = { goal, amount -> viewModel.updateGoalProgress(goal, amount) }
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

@Composable
fun GoalsContent(
    goals: List<GoalEntity>,
    onAddClick: () -> Unit,
    onDelete: (GoalEntity) -> Unit,
    onContribute: (GoalEntity, Double) -> Unit
) {
    Scaffold(
        containerColor = BackgroundGray,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick, containerColor = PrimaryBlue, contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(20.dp)) {
            Text(
                text = "Savings Goals",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (goals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No goals set yet. Start planning your future!", color = TextSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(goals) { goal ->
                        GoalCard(goal, onDelete, onContribute)
                    }
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
    val progress = (goal.savedAmount / goal.targetAmount).toFloat()

    StandardCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Flag, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(goal.title, style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Target: $${String.format("%.2f", goal.targetAmount)}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            IconButton(onClick = { onDelete(goal) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WarningRed.copy(alpha = 0.6f))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Progress: ${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text("$${String.format("%.2f", goal.savedAmount)} saved", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = AccentGreen)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        CustomProgressBar(progress = progress, color = if (progress >= 1f) AccentGreen else PrimaryBlue)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = { showContributeDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("ADD FUNDS")
        }
    }

    if (showContributeDialog) {
        var amount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showContributeDialog = false },
            title = { Text("Contribute to ${goal.title}") },
            text = {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onContribute(goal, amt)
                    showContributeDialog = false
                }) {
                    Text("ADD")
                }
            },
            dismissButton = {
                TextButton(onClick = { showContributeDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onConfirm: (String, Double, Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Initialize New Goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val t = target.toDoubleOrNull() ?: 0.0
                if (title.isNotBlank() && t > 0) {
                    onConfirm(title, t, System.currentTimeMillis() + 2592000000L) // Default 30 days
                }
            }) {
                Text("INITIATE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GoalsScreenPreview() {
    SpendSenseTheme {
        GoalsContent(
            goals = listOf(
                GoalEntity(1, "New Car", 25000.0, 5000.0, 0L),
                GoalEntity(2, "Vacation", 3000.0, 2800.0, 0L)
            ),
            onAddClick = {},
            onDelete = {},
            onContribute = { _, _ -> }
        )
    }
}
