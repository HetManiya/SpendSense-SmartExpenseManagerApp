package com.spendsense.app.frontend.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.spendsense.app.backend.local.ExpenseEntity
import com.spendsense.app.frontend.components.GlassCard
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
    val userProfile by viewModel.userProfile.collectAsState()
    val currency = userProfile?.currencySymbol ?: "₹"

    AddEntryContent(
        existingExpense = existingExpense,
        currency = currency,
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
    currency: String,
    onSaveExpense: (Double, String, String, String) -> Unit,
    onSaveIncome: (Double, String, String) -> Unit,
    onBack: () -> Unit
) {
    var isExpense by remember { mutableStateOf(existingExpense != null || true) }
    var amount by remember { mutableStateOf(existingExpense?.amount?.toString() ?: "") }
    var note by remember { mutableStateOf(existingExpense?.note ?: "") }
    var selectedCategory by remember { mutableStateOf(existingExpense?.category ?: "Food") }
    var selectedPayment by remember { mutableStateOf(existingExpense?.paymentMethod ?: "Cash") }
    var showScanOptions by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    val themeColor = if (isExpense) WarningRed else AccentGreen

    val processImage = { uri: Uri ->
        isProcessing = true
        try {
            val image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Improved OCR Regex: Looks for decimal numbers, filters out common date patterns
                    val priceRegex = Regex("""\d{1,3}(?:[.,]\d{3})*[.,]\d{2}""")
                    val dateRegex = Regex("""\d{2,4}[./-]\d{2}[./-]\d{2,4}""")
                    
                    val filteredText = visionText.text.split("\n")
                        .filter { !dateRegex.containsMatchIn(it) }
                        .joinToString(" ")

                    val matches = priceRegex.findAll(filteredText)
                        .map { it.value.replace(",", "") }
                        .mapNotNull { it.toDoubleOrNull() }
                        .toList()
                    
                    if (matches.isNotEmpty()) {
                        val detectedAmount = matches.maxOrNull() ?: 0.0
                        amount = detectedAmount.toString()
                        Toast.makeText(context, "Amount detected: $currency$amount", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No amount detected. Please enter manually.", Toast.LENGTH_LONG).show()
                    }
                    isProcessing = false
                }
                .addOnFailureListener { 
                    Toast.makeText(context, "Scan failed: ${it.message}", Toast.LENGTH_LONG).show()
                    isProcessing = false 
                }
        } catch (e: Exception) { 
            Toast.makeText(context, "Error processing image", Toast.LENGTH_SHORT).show()
            isProcessing = false 
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) processImage(createTempPictureUri(context))
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { processImage(it) }
    }

    val expenseCategories = listOf("Food", "Travel", "Shopping", "Auto", "Rent", "Health", "Entertainment", "Others")
    val incomeSources = listOf("Salary", "Freelance", "Gift", "Investment", "Others")
    val payments = listOf("Cash", "Card", "UPI", "Bank Transfer")

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
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = if (existingExpense != null) "Edit Transaction" else "Add Transaction",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Surface(
                        onClick = { if (isExpense) showScanOptions = true },
                        modifier = Modifier.size(44.dp).alpha(if (isExpense) 1f else 0f),
                        shape = CircleShape,
                        color = PrimaryBlue.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.DocumentScanner, contentDescription = "Scan", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                        }
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
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Type Switcher
                Surface(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        TabItemModern("Expense", isExpense, { isExpense = true }, Modifier.weight(1f), WarningRed)
                        TabItemModern("Income", !isExpense, { isExpense = false }, Modifier.weight(1f), AccentGreen)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Amount Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Enter Amount", 
                            style = MaterialTheme.typography.labelLarge, 
                            color = TextSecondary, 
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currency,
                                style = MaterialTheme.typography.displayLarge,
                                color = themeColor.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold
                            )
                            TextField(
                                value = amount,
                                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                                placeholder = { Text("0", color = TextSecondary.copy(alpha = 0.2f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.displayLarge.copy(
                                    color = themeColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    letterSpacing = (-2).sp
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Details Card
                StandardCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Transaction Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(20.dp))

                    ModernCategorySelector(
                        label = if (isExpense) "Category" else "Source", 
                        selected = selectedCategory, 
                        options = if (isExpense) expenseCategories else incomeSources,
                        icon = Icons.Rounded.Category,
                        onSelect = { selectedCategory = it }
                    )
                    
                    if (isExpense) {
                        Spacer(modifier = Modifier.height(20.dp))
                        ModernCategorySelector(
                            label = "Payment Method", 
                            selected = selectedPayment, 
                            options = payments,
                            icon = Icons.Rounded.AccountBalanceWallet,
                            onSelect = { selectedPayment = it }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Add Note") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Lunch with friends") },
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Rounded.EditNote, contentDescription = null, tint = PrimaryBlue) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = DividerGray
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                PrimaryButton(
                    text = if (existingExpense != null) "Update Transaction" else "Save Transaction",
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            if (isExpense) onSaveExpense(amt, selectedCategory, note, selectedPayment)
                            else onSaveIncome(amt, selectedCategory, note)
                        } else {
                            Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    color = themeColor,
                    isLoading = isProcessing
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showScanOptions) {
        ModalBottomSheet(
            onDismissRequest = { showScanOptions = false },
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = DividerGray) }
        ) {
            Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
                Text("Smart Receipt Scan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text("AI will automatically detect the amount", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ScanModernOption(
                        title = "Camera",
                        icon = Icons.Rounded.PhotoCamera,
                        color = PrimaryBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { showScanOptions = false; cameraLauncher.launch(createTempPictureUri(context)) }
                    )
                    ScanModernOption(
                        title = "Gallery",
                        icon = Icons.Rounded.Collections,
                        color = AccentGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { showScanOptions = false; galleryLauncher.launch("image/*") }
                    )
                }
            }
        }
    }
}

@Composable
fun ScanModernOption(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun TabItemModern(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, selectedColor: Color) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) selectedColor else Color.Transparent,
        modifier = modifier.fillMaxHeight()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text, 
                color = if (selected) Color.White else TextSecondary, 
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernCategorySelector(label: String, selected: String, options: List<String>, icon: androidx.compose.ui.graphics.vector.ImageVector, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(icon, contentDescription = null, tint = PrimaryBlue) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = DividerGray
            )
        )
        ExposedDropdownMenu(
            expanded = expanded, 
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { sel ->
                DropdownMenuItem(
                    text = { Text(sel, fontWeight = FontWeight.Bold) }, 
                    onClick = { onSelect(sel); expanded = false }
                )
            }
        }
    }
}

private fun createTempPictureUri(context: Context): Uri {
    val tempFile = File.createTempFile("receipt_", ".jpg", context.externalCacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
}
