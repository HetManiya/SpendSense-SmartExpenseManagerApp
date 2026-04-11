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
    var showScanOptions by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    
    // OCR Processing function
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
                    
                    if (matches.isNotEmpty()) {
                        amount = matches.maxOrNull().toString()
                    } else {
                        val numbers = visionText.text.split("\\s+".toRegex())
                            .mapNotNull { it.replace(",", "").toDoubleOrNull() }
                        if (numbers.isNotEmpty()) {
                            amount = numbers.maxOrNull().toString()
                        }
                    }
                    isProcessing = false
                }
                .addOnFailureListener {
                    isProcessing = false
                }
        } catch (e: Exception) {
            isProcessing = false
        }
    }

    // Camera Launcher
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let { processImage(it) }
        }
    }

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { processImage(it) }
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
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
            // Transaction Type Toggle
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite
            ) {
                Row(modifier = Modifier.padding(6.dp)) {
                    TabItem(
                        text = "Expense",
                        selected = isExpense,
                        onClick = { isExpense = true },
                        modifier = Modifier.weight(1f),
                        selectedColor = WarningRed
                    )
                    TabItem(
                        text = "Income",
                        selected = !isExpense,
                        onClick = { isExpense = false },
                        modifier = Modifier.weight(1f),
                        selectedColor = AccentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            StandardCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Amount", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextField(
                                value = amount,
                                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                                placeholder = { Text("0.00", color = TextSecondary.copy(alpha = 0.3f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.displayMedium.copy(
                                    color = if (isExpense) WarningRed else AccentGreen,
                                    fontWeight = FontWeight.Bold
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                            if (isProcessing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                    
                    if (isExpense) {
                        Surface(
                            onClick = { showScanOptions = true },
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryVariant,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.DocumentScanner, 
                                contentDescription = "Scan Receipt", 
                                tint = PrimaryBlue,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                if (isExpense) {
                    CategorySelector(
                        label = "Category",
                        selected = selectedCategory,
                        options = expenseCategories,
                        onSelect = { selectedCategory = it }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    CategorySelector(
                        label = "Payment Method",
                        selected = selectedPayment,
                        options = payments,
                        onSelect = { selectedPayment = it }
                    )
                } else {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { selectedCategory = it },
                        label = { Text("Source") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGreen,
                            unfocusedBorderColor = DividerGray
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Description", color = TextSecondary.copy(alpha = 0.5f)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = DividerGray
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

    if (showScanOptions) {
        ModalBottomSheet(
            onDismissRequest = { showScanOptions = false },
            containerColor = SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "Scan Receipt",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                ListItem(
                    headlineContent = { Text("Camera") },
                    leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showScanOptions = false
                        val uri = createTempPictureUri(context)
                        tempPhotoUri = uri
                        cameraLauncher.launch(uri)
                    }
                )
                ListItem(
                    headlineContent = { Text("Gallery") },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showScanOptions = false
                        galleryLauncher.launch("image/*")
                    }
                )
            }
        }
    }
}

private fun createTempPictureUri(context: Context): Uri {
    val tempFile = File.createTempFile("receipt_", ".jpg", context.externalCacheDir)
    return FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        "${context.packageName}.fileprovider",
        tempFile
    )
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
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) SurfaceWhite else TextSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
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
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = DividerGray
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SurfaceWhite)
        ) {
            options.forEach { sel ->
                DropdownMenuItem(
                    text = { Text(sel, color = TextPrimary) },
                    onClick = { onSelect(sel); expanded = false }
                )
            }
        }
    }
}
