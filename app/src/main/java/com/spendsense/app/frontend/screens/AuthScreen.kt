package com.spendsense.app.frontend.screens

import android.app.Activity
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.spendsense.app.BuildConfig
import com.spendsense.app.frontend.components.PrimaryButton
import com.spendsense.app.frontend.components.StandardCard
import com.spendsense.app.frontend.theme.*
import com.spendsense.app.frontend.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: MainViewModel,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Navigation on Success
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            // If profile exists, go to Home, otherwise Onboarding
            if (userProfile != null && userProfile!!.name.isNotEmpty()) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            } else {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            }
        }
    }

    // Show Snackbar on Error
    LaunchedEffect(authError) {
        authError?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearAuthError()
        }
    }

    // Configure Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()
    }
    
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    viewModel.signInWithGoogle(idToken)
                }
            } catch (e: ApiException) {
                // Error handled via viewModel.authError
            }
        }
    }

    fun validateInputs(): Boolean {
        if (email.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("Please enter email") }
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            scope.launch { snackbarHostState.showSnackbar("Please enter a valid email") }
            return false
        }
        if (password.length < 6) {
            scope.launch { snackbarHostState.showSnackbar("Password must be at least 6 characters") }
            return false
        }
        return true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SurfaceWhite, BackgroundGray)
                )
            )
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                
                // Logo Section
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = PrimaryBlue,
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(45.dp),
                            tint = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "SpendSense",
                    style = MaterialTheme.typography.displaySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                )
                
                Text(
                    text = "Smart. Simple. Secure.",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(40.dp))

                StandardCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = isSignUp,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { 20 }))
                                .with(fadeOut(animationSpec = tween(300)))
                        },
                        label = "auth_form"
                    ) { targetIsSignUp ->
                        Column {
                            Text(
                                text = if (targetIsSignUp) "Create Account" else "Welcome Back",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (targetIsSignUp) "Start your journey to financial freedom" else "Sign in to continue your progress",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = PrimaryBlue.copy(alpha = 0.6f)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = DividerGray
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = PrimaryBlue.copy(alpha = 0.6f)) },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    val image = if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(imageVector = image, contentDescription = null, tint = TextSecondary)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = DividerGray
                                )
                            )
                            
                            if (!targetIsSignUp) {
                                TextButton(
                                    onClick = { /* Forgot Password */ },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Forgot Password?", style = MaterialTheme.typography.labelLarge, color = PrimaryBlue)
                                }
                            } else {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            PrimaryButton(
                                text = if (targetIsSignUp) "Register" else "Login",
                                onClick = {
                                    if (validateInputs()) {
                                        if (targetIsSignUp) {
                                            viewModel.signUpWithEmail(email, password)
                                        } else {
                                            viewModel.signInWithEmail(email, password)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                isLoading = isAuthLoading
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = DividerGray)
                    Text(
                        text = " OR ",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = DividerGray)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Social Login Button
                OutlinedButton(
                    onClick = { launcher.launch(googleSignInClient.signInIntent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
                    enabled = !isAuthLoading
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_info_details),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = { isSignUp = !isSignUp }) {
                    Text(
                        text = if (isSignUp) "Already have an account? Login" else "New to SpendSense? Create Account",
                        style = MaterialTheme.typography.bodyLarge,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))

                // Trust Section
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    color = PrimaryBlue.copy(alpha = 0.05f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bank-Grade 256-bit Encryption",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "SpendSense respects your privacy. We never sell your financial data.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
