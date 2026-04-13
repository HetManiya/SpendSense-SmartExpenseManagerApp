package com.spendsense.app

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.spendsense.app.frontend.screens.MainScreen
import com.spendsense.app.frontend.theme.SpendSenseTheme
import com.spendsense.app.frontend.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.FragmentActivity

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(), Color.Transparent.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(), Color.Transparent.toArgb()
            )
        )
        super.onCreate(savedInstanceState)
        
        setContent {
            SpendSenseTheme {
                MainScreen(viewModel = mainViewModel)
            }
        }
    }
}
