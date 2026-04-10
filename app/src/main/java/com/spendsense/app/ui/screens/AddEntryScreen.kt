package com.spendsense.app.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.spendsense.app.data.local.ExpenseEntity
import com.spendsense.app.ui.components.PrimaryButton
import com.spendsense.app.ui.components.StandardCard
import com.spendsense.app.ui.theme.*
import com.spendsense.app.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(navController: NavController, viewModel: MainViewModel, expenseId: Int = -1) {
    val expenses by viewModel.allExpenses.collectAsState()
    val existingExpense = if (expenseId != -1) expenses.find { it.id == expenseId } else null
    val context = LocalContext.current

    AddEntryContent(
        existingExpense = existingExpense,
        onSaveExpense = { amt, cat, note, pay -> 
            viewModel.addOrUpdateExpense(
                id = existingExpense?.id ?: 0,
                amount = amt,
                category = cat,
                note = note,
                paymentMethod = pay,
                date = existingExpense?.date
            )
            navController.popBackStack() 
        },
        onSaveIncome = { amt, src, note -> 
            viewModel.addOrUpdateIncome(
                amount = amt,
                source = src,
                note = note
            )
            navController.popBackStack() 
        },
        onBack = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryContent(
    existingExpense: ExpenseEntity? = null,
    onSaveExpense: (Double, String, String, String) -> Unit,
    onSaveIncome: (Double, String, String) -> Unit,
    onBack: () -> Unit
) {
    var isExpense by remember { mutableStateOf(existingExpense != null || true) }
    var amount by remember { mutableStateOf(existingExpense?.amount?.toString() ?: "") }
    var note by remember { mutableStateOf(existingExpense?.note ?: "") }
    var selectedCategory by remember { mutableStateOf(existingExpense?.category ?: "Auto") }
    var selectedPayment by remember { mutableStateOf(existingExpense?.paymentMethod ?: "Cash") }
    
    val context = LocalContext.current
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val image = InputImage.fromFilePath(context, it)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Simple logic to find the largest number which might be the total
                    val numbers = visionText.text.split("\\s+".toRegex())
                        .mapNotNull { it.replace(",", "").toDoubleOrNull() }
                    if (numbers.isNotEmpty()) {
                        amount = numbers.maxOrNull().toString()
                    }
                }
        }
    }

    val expenseCategories = listOf("Auto", "Food", "Travel", "Shopping", "Rent", "Health", "Entertainment", "Others")
    val payments = listOf("Cash", "Card", "UPI")

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (existingExpense != null) "Edit Entry" else "New Entry", 
                        style = MaterialTheme.typography.titleLarge, 
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize()
        ) {
            if (existingExpense == null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row {
                        FilterChip(
                            selected = isExpense,
                            onClick = { isExpense = true },
                            label = { Text("Expense") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WarningRed.copy(alpha = 0.1f),
                                selectedLabelColor = WarningRed,
                                labelColor = TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        FilterChip(
                            selected = !isExpense,
                            onClick = { isExpense = false },
                            label = { Text("Income") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentGreen.copy(alpha = 0.1f),
                                selectedLabelColor = AccentGreen,
                                labelColor = TextSecondary
                            )
                        )
                    }
                    
                    if (isExpense) {
                        IconButton(onClick = { launcher.launch("image/*") }) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = "Scan Receipt", tint = PrimaryBlue)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            StandardCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = if (isExpense) WarningRed else AccentGreen,
                        fontWeight = FontWeight.Bold
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isExpense) WarningRed else AccentGreen,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                    )
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                if (isExpense) {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(SurfaceWhite)
                        ) {
                            expenseCategories.forEach { sel ->
                                DropdownMenuItem(
                                    text = { Text(sel, color = TextPrimary) },
                                    onClick = { selectedCategory = sel; expanded = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    var paymentExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = paymentExpanded, onExpandedChange = { paymentExpanded = !paymentExpanded }) {
                        OutlinedTextField(
                            value = selectedPayment,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Payment Method") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = paymentExpanded,
                            onDismissRequest = { paymentExpanded = false },
                            modifier = Modifier.background(SurfaceWhite)
                        ) {
                            payments.forEach { sel ->
                                DropdownMenuItem(
                                    text = { Text(sel, color = TextPrimary) },
                                    onClick = { selectedPayment = sel; paymentExpanded = false }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { selectedCategory = it },
                        label = { Text("Source") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGreen,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("What was this for?", color = TextSecondary.copy(alpha = 0.5f)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            PrimaryButton(
                text = if (existingExpense != null) "Update Transaction" else "Save Transaction",
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        if (isExpense) {
                            onSaveExpense(amt, selectedCategory, note, selectedPayment)
                        } else {
                            onSaveIncome(amt, selectedCategory, note)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                color = if (isExpense) WarningRed else AccentGreen
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEntryScreenPreview() {
    SpendSenseTheme {
        AddEntryContent(
            onSaveExpense = { _, _, _, _ -> },
            onSaveIncome = { _, _, _ -> },
            onBack = {}
        )
    }
}
