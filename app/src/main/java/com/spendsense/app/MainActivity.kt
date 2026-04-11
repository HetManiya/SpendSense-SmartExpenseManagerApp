package com.spendsense.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.spendsense.app.frontend.screens.AuthScreen
import com.spendsense.app.frontend.screens.MainScreen
import com.spendsense.app.frontend.theme.SpendSenseTheme
import com.spendsense.app.frontend.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.FragmentActivity

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SpendSenseTheme {
                val isAuthenticated by viewModel.isAuthenticated.collectAsState()
                val isGuest by viewModel.isGuest.collectAsState()
                
                if (isAuthenticated || isGuest) {
                    MainScreen(viewModel = viewModel)
                } else {
                    AuthScreen(
                        onLoginSuccess = { idToken ->
                            viewModel.signInWithGoogle(idToken)
                        },
                        onSkip = {
                            viewModel.setGuestMode(true)
                        }
                    )
                }
            }
        }
    }
}
