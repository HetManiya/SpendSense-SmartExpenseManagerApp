package com.spendsense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.spendsense.app.data.local.SpendSenseDatabase
import com.spendsense.app.data.repository.FinanceRepository
import com.spendsense.app.data.repository.SmartInsightsRepository
import com.spendsense.app.ui.screens.AuthScreen
import com.spendsense.app.ui.screens.MainScreen
import com.spendsense.app.ui.screens.SpendSenseNavHost
import com.spendsense.app.ui.theme.SpendSenseTheme
import com.spendsense.app.ui.viewmodels.MainViewModel
import com.spendsense.app.ui.viewmodels.MainViewModelFactory
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = SpendSenseDatabase.getDatabase(applicationContext)
        val financeRepository = FinanceRepository(database.spendSenseDao())
        val smartInsightsRepository = SmartInsightsRepository()
        
        val factory = MainViewModelFactory(financeRepository, smartInsightsRepository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setContent {
            SpendSenseTheme {
                val isAuthenticated by viewModel.isAuthenticated.collectAsState()
                val isGuest by viewModel.isGuest.collectAsState()
                
                if (isAuthenticated || isGuest) {
                    MainScreen(viewModel = viewModel)
                } else {
                    AuthScreen(
                        onLoginSuccess = {
                            viewModel.setAuthenticated(true)
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
