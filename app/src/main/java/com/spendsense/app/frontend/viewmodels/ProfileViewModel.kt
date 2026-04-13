package com.spendsense.app.frontend.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.spendsense.app.backend.local.BudgetEntity
import com.spendsense.app.backend.local.UserEntity
import com.spendsense.app.backend.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    val userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun saveUserProfile(name: String, currency: String, budgetLimit: Double, isGuest: Boolean) {
        val currentMonthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            repository.updateUserProfile(UserEntity(id = 1, name = name, currencySymbol = currency))
            repository.setBudget(BudgetEntity(month = currentMonthYear, limitAmount = budgetLimit))
            
            if (!isGuest) {
                val userId = auth.currentUser?.uid ?: return@launch
                val profile = mapOf("name" to name, "currency" to currency, "budget" to budgetLimit)
                firestore.collection("users").document(userId).set(profile, com.google.firebase.firestore.SetOptions.merge())
            }
        }
    }
    
    fun migrateGuestDataToFirebase(onComplete: () -> Unit) {
        // Implementation for Suggestion #7
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            // 1. Get all local data
            // 2. Upload to Firestore
            // 3. onComplete()
        }
    }
}
