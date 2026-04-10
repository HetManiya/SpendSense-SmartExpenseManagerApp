package com.spendsense.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spendsense.app.ui.components.GlowButton
import com.spendsense.app.ui.theme.*
import com.spendsense.app.ui.viewmodels.MainViewModel

@Composable
fun ProfileSetupScreen(navController: NavController, viewModel: MainViewModel) {
    ProfileSetupContent(
        onGetStarted = { name, currency, budget, incomeRange ->
            viewModel.saveUserProfile(name, currency, budget, incomeRange)
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Onboarding.route) { inclusive = true }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupContent(
    onGetStarted: (String, String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("$") }
    var budget by remember { mutableStateOf("") }
    var incomeRange by remember { mutableStateOf("Medium") }
    
    val ranges = listOf("Low", "Medium", "High")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSlate)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "IDENTITY INITIALIZATION",
            style = MaterialTheme.typography.labelSmall,
            color = CyanGlow
        )
        Text(
            text = "Welcome to SpendSense",
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("OPERATOR NAME", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = GlassWhite
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Income Range Picker
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = incomeRange,
                onValueChange = {},
                readOnly = true,
                label = { Text("INCOME RANGE", color = TextSecondary) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = GlassWhite
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(CardBackground)
            ) {
                ranges.forEach { range ->
                    DropdownMenuItem(
                        text = { Text(range, color = TextPrimary) },
                        onClick = { incomeRange = range; expanded = false }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = currency,
            onValueChange = { currency = it },
            label = { Text("CURRENCY SYMBOL", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = GlassWhite
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = budget,
            onValueChange = { budget = it },
            label = { Text("MONTHLY BUDGET CAP", color = TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = GlassWhite
            )
        )
        Spacer(modifier = Modifier.height(32.dp))

        GlowButton(
            text = "INITIALIZE SYSTEM",
            onClick = {
                val budgetValue = budget.toDoubleOrNull()
                if(name.isNotBlank() && budgetValue != null) {
                    onGetStarted(name, currency, budgetValue, incomeRange)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSetupScreenPreview() {
    SpendSenseTheme {
        ProfileSetupContent(onGetStarted = { _, _, _, _ -> })
    }
}
