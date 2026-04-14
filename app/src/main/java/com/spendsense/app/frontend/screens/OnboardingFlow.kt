package com.spendsense.app.frontend.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spendsense.app.frontend.components.PrimaryButton
import com.spendsense.app.frontend.components.StandardCard
import com.spendsense.app.frontend.theme.*
import com.spendsense.app.frontend.viewmodels.OnboardingViewModel

data class ProfessionItem(val name: String, val icon: ImageVector)

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
            Column(modifier = Modifier.statusBarsPadding()) {
                LinearProgressIndicator(
                    progress = { uiState.currentProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = PrimaryBlue,
                    trackColor = DividerGray.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Step $currentStep of 3",
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    TextButton(onClick = onFinish) {
                        Text("Skip", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = BackgroundGray
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(
            brush = Brush.verticalGradient(
                colors = listOf(BackgroundGray, SurfaceWhite)
            )
        )) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                    }.using(SizeTransform(clip = false))
                },
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    1 -> GatewayScreenModern(
                        viewModel = viewModel,
                        onNext = {
                            viewModel.updateProgress(0.66f)
                            currentStep = 2
                        }
                    )
                    2 -> ProfessionScreenModern(
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
                    3 -> PersonalizationScreenModern(
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
}

@Composable
fun GatewayScreenModern(viewModel: OnboardingViewModel, onNext: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(100.dp).shadow(12.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = PrimaryBlue
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "Start Your Journey", 
            style = MaterialTheme.typography.headlineLarge, 
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            letterSpacing = (-1).sp
        )
        Text(
            "AI-powered insights for smarter spending.", 
            color = TextSecondary, 
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        StandardCard(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = PrimaryBlue) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = TextSecondary)
                    }
                },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = PrimaryBlue) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = "Continue Journey",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            enabled = uiState.email.isNotEmpty() && uiState.password.length >= 6
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProfessionScreenModern(viewModel: OnboardingViewModel, onNext: () -> Unit, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val professions = listOf(
        ProfessionItem("Student", Icons.Rounded.School),
        ProfessionItem("Professional", Icons.Rounded.Work),
        ProfessionItem("Freelancer", Icons.Rounded.LaptopMac),
        ProfessionItem("Business", Icons.Rounded.Business),
        ProfessionItem("Home Maker", Icons.Rounded.Home),
        ProfessionItem("Retired", Icons.Rounded.AccountBox)
    )

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            "Who are you?", 
            style = MaterialTheme.typography.headlineLarge, 
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            letterSpacing = (-1).sp
        )
        Text("Tailoring insights to your lifestyle.", color = TextSecondary, modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(professions) { item ->
                val isSelected = uiState.selectedProfession == item.name
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clickable { viewModel.updateProfession(item.name) }
                        .shadow(if (isSelected) 8.dp else 0.dp, RoundedCornerShape(24.dp), spotColor = PrimaryBlue.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(24.dp),
                    color = if (isSelected) PrimaryBlue else Color.White,
                    border = if (!isSelected) BorderStroke(1.dp, DividerGray.copy(alpha = 0.5f)) else null
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = if (isSelected) Color.White.copy(alpha = 0.2f) else PrimaryBlue.copy(alpha = 0.05f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = item.name,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(60.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, DividerGray)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Back", fontWeight = FontWeight.Bold, color = TextSecondary)
                }
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
fun PersonalizationScreenModern(viewModel: OnboardingViewModel, onFinish: () -> Unit, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var showHelp by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            "Final Touches", 
            style = MaterialTheme.typography.headlineLarge, 
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            letterSpacing = (-1).sp
        )
        Text("Define your initial budget goals.", color = TextSecondary, modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(32.dp))

        StandardCard(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.username,
                onValueChange = { viewModel.updateUsername(it) },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Rounded.Face, contentDescription = null, tint = PrimaryBlue) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = uiState.budget,
                onValueChange = { viewModel.updateBudget(it) },
                label = { Text("Monthly Budget Goal") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                prefix = { Text("₹") },
                leadingIcon = { Icon(Icons.Rounded.TrackChanges, contentDescription = null, tint = PrimaryBlue) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = BackgroundGray.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.QuestionMark, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Budget Calculator Help", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = showHelp, 
                        onCheckedChange = { showHelp = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryBlue)
                    )
                }
            }

            AnimatedVisibility(visible = showHelp) {
                Surface(
                    color = PrimaryBlue.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("50/30/20 Budgeting Rule", fontWeight = FontWeight.ExtraBold, color = PrimaryBlue, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Allocate 50% for Needs, 30% for Wants, and 20% for Savings. AI will help you stay on track!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(60.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, DividerGray)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Back", fontWeight = FontWeight.Bold, color = TextSecondary)
                }
            }
            PrimaryButton(
                text = "Get Started",
                onClick = onFinish,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
