package com.spendsense.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsense.app.ui.components.GlowButton
import com.spendsense.app.ui.theme.*

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit, onSkip: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSlate),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "ACCESS PROTOCOL",
                style = MaterialTheme.typography.labelSmall,
                color = CyanGlow
            )
            Text(
                text = "SPENDSENSE",
                style = MaterialTheme.typography.displayLarge,
                color = ElectricBlue,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Text(
                text = "Secure authentication required to sync encrypted financial data with the cloud matrix.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            GlowButton(
                text = "SIGN IN WITH GOOGLE",
                onClick = onLoginSuccess,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onSkip) {
                Text(
                    text = "SKIP TO OFFLINE MODE",
                    color = CyanGlow.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "v1.0.4_SECURE_NODE",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    SpendSenseTheme {
        AuthScreen(onLoginSuccess = {}, onSkip = {})
    }
}
