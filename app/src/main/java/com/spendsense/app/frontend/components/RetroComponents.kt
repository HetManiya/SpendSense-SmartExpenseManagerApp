package com.spendsense.app.frontend.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.spendsense.app.frontend.theme.PrimaryBlue

/**
 * DEPRECATED: These components are being phased out in favor of StandardComponents.kt
 * and Material 3 defaults. Use StandardCard and PrimaryButton instead.
 */

@Composable
@Deprecated("Use PrimaryButton instead", ReplaceWith("PrimaryButton(text, onClick, modifier)"))
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = PrimaryBlue
) {
    PrimaryButton(text = text, onClick = onClick, modifier = modifier, color = color)
}

@Composable
@Deprecated("Use CustomProgressBar instead", ReplaceWith("CustomProgressBar(progress, modifier, color)"))
fun RetroProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = PrimaryBlue
) {
    CustomProgressBar(progress = progress, modifier = modifier, color = color)
}
