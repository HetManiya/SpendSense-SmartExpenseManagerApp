package com.spendsense.app.frontend.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.spendsense.app.backend.local.ExpenseEntity
import com.spendsense.app.frontend.components.PrimaryButton
import com.spendsense.app.frontend.components.StandardCard
import com.spendsense.app.frontend.theme.*
import com.spendsense.app.frontend.viewmodels.MainViewModel
import java.io.File
import java.util.Objects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(navController: NavController, viewModel: MainViewModel, expenseId: Int = -1) {
    val expenses by viewModel.allExpenses.collectAsState()
    val existingExpense = if (expenseId != -1) expenses.find { it.id == expenseId } else null

    AddEntryContent(
        existingExpense = existingExpense,
        viewModel = viewModel,
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
    viewModel: MainViewModel,
    onSaveExpense: (Double, String, String, String) -> Unit,
    onSaveIncome: (Double, String, String) -> Unit,
    onBack: () -> Unit
) {
    var isExpense by remember { mutableStateOf(existingExpense != null || true) }
    var amount by remember { mutableStateOf(existingExpense?.amount?.toString() ?: "") }
    var note by remember { mutableStateOf(existingExpense?.note ?: "") }
    var selectedCategory by remember { mutableStateOf(existingExpense?.category ?: "Auto") }
    var selectedPayment by remember { mutableStateOf(existingExpense?.paymentMethod ?: "Cash") }
    var showScanOptions by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var isAutoSuggested by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    
    // Predictive Category Logic
    LaunchedEffect(note) {
        if (isExpense && (selectedCategory == "Auto" || isAutoSuggested) && note.length > 2) {
            // This is a placeholder since we can't directly call repository from UI, 
            // in a real app this would be a ViewModel function.
            // For now, I'll simulate the "Auto" logic.
            val prediction = when {
                note.lowercase().contains("starbucks") || note.lowercase().contains("food") -> "Food"
                note.lowercase().contains("uber") || note.lowercase().contains("ola") -> "Travel"
                note.lowercase().contains("amazon") || note.lowercase().contains("zara") -> "Shopping"
                else -> "Auto"
            }
            if (prediction != "Auto") {
                selectedCategory = prediction
                isAutoSuggested = true
            }
        }
    }

    // OCR Processing
    val processImage = { uri: Uri ->
        isProcessing = true
        try {
            val image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val priceRegex = Regex("""\d{1,3}(?:[.,]\d{3})*[.,]\d{2}""")
                    val matches = priceRegex.findAll(visionText.text)
                        .map { it.value.replace(",", "") }
                        .mapNotNull { it.toDoubleOrNull() }
                        .toList()
                    
                    if (matches.isNotEmpty()) amount = matches.maxOrNull().toString()
                    isProcessing = false
                }
                .addOnFailureListener { isProcessing = false }
        } catch (e: Exception) { isProcessing = false }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) processImage(createTempPictureUri(context))
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { processImage(it) }
    }

    val expenseCategories = listOf("Auto", "Food", "Travel", "Shopping", "Rent", "Health", "Entertainment", "Others")
    val payments = listOf("Cash", "Card", "UPI")

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            TopAppBar(
                title = { Text(if (existingExpense != null) "Edit Entry" else "New Entry", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(20.dp).fillMaxSize()) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = SurfaceWhite) {
                Row(modifier = Modifier.padding(6.dp)) {
                    TabItem("Expense", isExpense, { isExpense = true }, Modifier.weight(1f), WarningRed)
                    TabItem("Income", !isExpense, { isExpense = false }, Modifier.weight(1f), AccentGreen)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            StandardCard(modifier = Modifier.fillMaxWidth()) {
                Text("Amount", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = amount,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.displayMedium.copy(
                            color = if (isExpense) WarningRed else AccentGreen,
                            fontWeight = FontWeight.Bold
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    if (isExpense) {
                        IconButton(onClick = { showScanOptions = true }) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = "Scan", tint = PrimaryBlue)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                if (isExpense) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            CategorySelector("Category", selectedCategory, expenseCategories) { 
                                selectedCategory = it 
                                isAutoSuggested = false 
                            }
                        }
                        if (isAutoSuggested) {
                            Icon(
                                Icons.Rounded.AutoAwesome, 
                                contentDescription = "AI Suggested", 
                                tint = PrimaryBlue,
                                modifier = Modifier.padding(start = 8.dp).size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    CategorySelector("Payment Method", selectedPayment, payments) { selectedPayment = it }
                } else {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { selectedCategory = it },
                        label = { Text("Source") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Starbucks Coffee") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            PrimaryButton(
                text = "Save Transaction",
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        if (isExpense) onSaveExpense(amt, selectedCategory, note, selectedPayment)
                        else onSaveIncome(amt, selectedCategory, note)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                color = if (isExpense) WarningRed else AccentGreen
            )
        }
    }

    if (showScanOptions) {
        ModalBottomSheet(onDismissRequest = { showScanOptions = false }) {
            Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                Text("Scan Receipt", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                ListItem(
                    headlineContent = { Text("Camera") },
                    leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                    modifier = Modifier.clickable { showScanOptions = false; cameraLauncher.launch(createTempPictureUri(context)) }
                )
                ListItem(
                    headlineContent = { Text("Gallery") },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                    modifier = Modifier.clickable { showScanOptions = false; galleryLauncher.launch("image/*") }
                )
            }
        }
    }
}

private fun createTempPictureUri(context: Context): Uri {
    val tempFile = File.createTempFile("receipt_", ".jpg", context.externalCacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
}

@Composable
fun TabItem(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, selectedColor: Color) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) selectedColor else Color.Transparent,
        modifier = modifier.height(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = if (selected) Color.White else TextSecondary, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { sel ->
                DropdownMenuItem(text = { Text(sel) }, onClick = { onSelect(sel); expanded = false })
            }
        }
    }
}
