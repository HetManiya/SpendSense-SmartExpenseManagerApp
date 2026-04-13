package com.spendsense.app.frontend.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spendsense.app.frontend.components.PrimaryButton
import com.spendsense.app.frontend.theme.*
import com.spendsense.app.frontend.viewmodels.OnboardingViewModel

@Composable
fun OnboardingFlow(
    navController: NavController,
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            Column {
                LinearProgressIndicator(
                    progress = { uiState.currentProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = PrimaryBlue,
                    trackColor = DividerGray.copy(alpha = 0.3f)
                )
            }
        },
        containerColor = BackgroundGray
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (currentStep) {
                1 -> GatewayScreen(
                    viewModel = viewModel,
                    onNext = {
                        viewModel.updateProgress(0.66f)
                        currentStep = 2
                    }
                )
                2 -> ProfessionScreen(
                    viewModel = viewModel,
                    onNext = {
                        viewModel.updateProgress(1.0f)
                        currentStep = 3
                    },
                    onBack = {
                        viewModel.updateProgress(0.33f)
                        currentStep = 1
                    }
                )
                3 -> PersonalizationScreen(
                    viewModel = viewModel,
                    onFinish = onFinish,
                    onBack = {
                        viewModel.updateProgress(0.66f)
                        currentStep = 2
                    }
                )
            }
        }
    }
}

@Composable
fun GatewayScreen(viewModel: OnboardingViewModel, onNext: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to SpendSense", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Start your journey to financial freedom", color = TextSecondary, modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Email or Mobile") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = "Continue",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.email.isNotEmpty() && uiState.password.length >= 6
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(" OR ", color = TextSecondary, modifier = Modifier.padding(horizontal = 8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { /* Google Sign In */ },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DividerGray)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Login, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with Google", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class ProfessionItem(val name: String, val icon: ImageVector)

@Composable
fun ProfessionScreen(viewModel: OnboardingViewModel, onNext: () -> Unit, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val professions = listOf(
        ProfessionItem("Student", Icons.Default.School),
        ProfessionItem("Professional", Icons.Default.Work),
        ProfessionItem("Freelancer", Icons.Default.Laptop),
        ProfessionItem("Business", Icons.Default.Business),
        ProfessionItem("Home Maker", Icons.Default.Home),
        ProfessionItem("Other", Icons.Default.Category)
    )

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("What do you do?", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Select your professional role", color = TextSecondary, modifier = Modifier.padding(bottom = 32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(professions) { item ->
                val isSelected = uiState.selectedProfession == item.name
                Surface(
                    modifier = Modifier.fillMaxWidth().height(120.dp).clickable { viewModel.updateProfession(item.name) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) PrimaryBlue else SurfaceWhite,
                    border = BorderStroke(1.dp, if (isSelected) PrimaryBlue else DividerGray)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else PrimaryBlue,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.name,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back")
            }
            PrimaryButton(
                text = "Next",
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = uiState.selectedProfession.isNotEmpty()
            )
        }
    }
}

@Composable
fun PersonalizationScreen(viewModel: OnboardingViewModel, onFinish: () -> Unit, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var showHelp by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Personalize", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Tell us a bit more about yourself", color = TextSecondary, modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(
            value = uiState.username,
            onValueChange = { viewModel.updateUsername(it) },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.budget,
            onValueChange = { viewModel.updateBudget(it) },
            label = { Text("Monthly Budget (₹)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            prefix = { Text("₹") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Help me calculate", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = showHelp, onCheckedChange = { showHelp = it })
        }

        AnimatedVisibility(visible = showHelp) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryVariant),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("The 50/30/20 Rule", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    Text(
                        "Allocate 50% of income to Needs, 30% to Wants, and 20% to Savings/Debt.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextButton(onClick = onFinish, modifier = Modifier.weight(1f).height(56.dp)) {
                Text("Skip", color = TextSecondary)
            }
            PrimaryButton(
                text = "Finish",
                onClick = onFinish,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
