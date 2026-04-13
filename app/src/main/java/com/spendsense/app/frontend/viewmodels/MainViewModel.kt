package com.spendsense.app.frontend.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.spendsense.app.backend.local.*
import com.spendsense.app.backend.repository.FinanceRepository
import com.spendsense.app.backend.repository.SmartInsightsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val smartInsights: SmartInsightsRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private var firestoreListener: ListenerRegistration? = null

    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _isGuest = MutableStateFlow(false)
    val isGuest: StateFlow<Boolean> = _isGuest

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading

    private val _groupId = MutableStateFlow<String?>(null)
    val groupId: StateFlow<String?> = _groupId

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    val allExpenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allIncomes = repository.allIncomes.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val allGoals = repository.allGoals.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val currentMonthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    
    private val _aiInsights = MutableStateFlow<List<String>>(emptyList())
    val aiInsights: StateFlow<List<String>> = _aiInsights

    private val _suggestedBudget = MutableStateFlow(0.0)
    val suggestedBudget: StateFlow<Double> = _suggestedBudget

    val dashboardState = combine(allExpenses, allIncomes, repository.getBudgetForMonth(currentMonthYear)) { expenses, incomes, budget ->
        val currentMonthExpenses = expenses.filter { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.date)) == currentMonthYear }
        val totalExpense = currentMonthExpenses.sumOf { it.amount }
        val totalIncome = incomes.filter { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.date)) == currentMonthYear }.sumOf { it.amount }
        val balance = totalIncome - totalExpense
        
        DashboardState(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance,
            budgetLimit = budget?.limitAmount ?: 0.0,
            recentExpenses = expenses.take(10)
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, DashboardState())

    init {
        auth.currentUser?.let {
            loadUserGroup(it.uid)
        }
        generateBudgetSuggestion()
    }

    private fun generateBudgetSuggestion() {
        viewModelScope.launch {
            allExpenses.collect { expenses ->
                _suggestedBudget.value = smartInsights.suggestBudgetGoal(expenses)
            }
        }
    }

    private fun loadUserGroup(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                _groupId.value = doc.getString("groupId")
                _groupId.value?.let { startFirestoreSync(it) }
            }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                _isAuthenticated.value = true
                _isGuest.value = false
                auth.currentUser?.let { loadUserGroup(it.uid) }
            } catch (e: Exception) {
                _authError.value = e.message ?: "Authentication failed"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val userId = result.user?.uid ?: throw Exception("User creation failed")
                
                // Initialize Firestore user document
                val profile = mapOf("name" to "", "currency" to "₹", "budget" to 0.0, "incomeRange" to "Medium")
                firestore.collection("users").document(userId).set(profile).await()
                
                _isAuthenticated.value = true
                _isGuest.value = false
            } catch (e: Exception) {
                _authError.value = e.message ?: "Sign up failed"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val userId = result.user?.uid ?: throw Exception("Google Sign-In failed")
                
                // Check if user exists in Firestore, if not initialize
                val doc = firestore.collection("users").document(userId).get().await()
                if (!doc.exists()) {
                    val profile = mapOf("name" to (result.user?.displayName ?: ""), "currency" to "₹", "budget" to 0.0, "incomeRange" to "Medium")
                    firestore.collection("users").document(userId).set(profile).await()
                }
                
                _isAuthenticated.value = true
                _isGuest.value = false
                loadUserGroup(userId)
            } catch (e: Exception) {
                _authError.value = e.message ?: "Authentication failed"
                Log.e("Auth", "Google sign in failed", e)
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun createGroup() {
        val userId = auth.currentUser?.uid ?: return
        val newGroupId = UUID.randomUUID().toString().substring(0, 8).uppercase()
        viewModelScope.launch {
            firestore.collection("users").document(userId).update("groupId", newGroupId)
                .addOnSuccessListener {
                    _groupId.value = newGroupId
                    startFirestoreSync(newGroupId)
                }
        }
    }

    fun joinGroup(id: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestore.collection("users").document(userId).update("groupId", id)
                .addOnSuccessListener {
                    _groupId.value = id
                    startFirestoreSync(id)
                }
        }
    }

    fun leaveGroup() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestore.collection("users").document(userId).update("groupId", null)
                .addOnSuccessListener {
                    _groupId.value = null
                    firestoreListener?.remove()
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // 1. Sign out from Firebase
            auth.signOut()
            
            // 2. Clear local Room database
            repository.clearAllData()
            
            // 3. Reset View State
            _isAuthenticated.value = false
            _isGuest.value = false
            _groupId.value = null
            firestoreListener?.remove()
        }
    }

    fun generateAiInsights() {
        viewModelScope.launch {
            val expenses = allExpenses.value.take(30)
            val insights = smartInsights.getThreeActionableTips(expenses)
            _aiInsights.value = insights
        }
    }

    fun addOrUpdateExpense(id: Int = 0, amount: Double, category: String, note: String, paymentMethod: String, date: Long? = null) {
        viewModelScope.launch {
            val finalCategory = if(category == "Auto" || category.isEmpty()) smartInsights.predictCategory(note) else category
            val expense = ExpenseEntity(
                id = id,
                amount = amount,
                category = finalCategory,
                date = date ?: System.currentTimeMillis(),
                note = note,
                paymentMethod = paymentMethod
            )
            repository.addExpense(expense)
            if (!isGuest.value) syncExpenseToFirebase(expense)
        }
    }

    private fun syncExpenseToFirebase(expense: ExpenseEntity) {
        val targetId = _groupId.value ?: auth.currentUser?.uid ?: return
        val collectionPath = if (_groupId.value != null) "groups" else "users"
        firestore.collection(collectionPath).document(targetId)
            .collection("expenses").document(expense.id.toString()).set(expense)
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            if (!isGuest.value) {
                val targetId = _groupId.value ?: auth.currentUser?.uid ?: return@launch
                val collectionPath = if (_groupId.value != null) "groups" else "users"
                firestore.collection(collectionPath).document(targetId)
                    .collection("expenses").document(expense.id.toString()).delete()
            }
        }
    }

    fun addOrUpdateIncome(id: Int = 0, amount: Double, source: String, note: String, date: Long? = null) {
        viewModelScope.launch {
            val income = IncomeEntity(
                id = id,
                amount = amount,
                date = date ?: System.currentTimeMillis(),
                source = source,
                note = note
            )
            repository.addIncome(income)
            if (!isGuest.value) syncIncomeToFirebase(income)
        }
    }

    private fun syncIncomeToFirebase(income: IncomeEntity) {
        val targetId = _groupId.value ?: auth.currentUser?.uid ?: return
        val collectionPath = if (_groupId.value != null) {
            "groups"
        } else {
            "users"
        }
        firestore.collection(collectionPath).document(targetId)
            .collection("incomes").document(income.id.toString()).set(income)
    }

    fun deleteIncome(income: IncomeEntity) {
        viewModelScope.launch {
            repository.deleteIncome(income)
            if (!isGuest.value) {
                val targetId = _groupId.value ?: auth.currentUser?.uid ?: return@launch
                val collectionPath = if (_groupId.value != null) "groups" else "users"
                firestore.collection(collectionPath).document(targetId)
                    .collection("incomes").document(income.id.toString()).delete()
            }
        }
    }

    fun saveUserProfile(name: String, currency: String, budgetLimit: Double, incomeRange: String = "") {
        viewModelScope.launch {
            repository.updateUserProfile(UserEntity(id = 1, name = name, currencySymbol = currency, incomeRange = incomeRange))
            repository.setBudget(BudgetEntity(month = currentMonthYear, limitAmount = budgetLimit))
            
            if (!isGuest.value) {
                val userId = auth.currentUser?.uid ?: return@launch
                val profile = mapOf("name" to name, "currency" to currency, "budget" to budgetLimit, "incomeRange" to incomeRange)
                firestore.collection("users").document(userId).set(profile, com.google.firebase.firestore.SetOptions.merge())
            }
        }
    }

    fun addGoal(title: String, target: Double, deadline: Long) {
        viewModelScope.launch {
            repository.addGoal(GoalEntity(title = title, targetAmount = target, deadline = deadline))
        }
    }

    fun updateGoalProgress(goal: GoalEntity, contribution: Double) {
        viewModelScope.launch {
            repository.addGoal(goal.copy(savedAmount = goal.savedAmount + contribution))
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    private fun startFirestoreSync(groupId: String) {
        firestoreListener?.remove()
        
        firestoreListener = firestore.collection("groups").document(groupId)
            .collection("expenses")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                
                viewModelScope.launch {
                    snapshots?.documents?.forEach { doc ->
                        val expense = doc.toObject(ExpenseEntity::class.java)
                        if (expense != null) {
                            repository.addExpense(expense)
                        }
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        firestoreListener?.remove()
    }
}

data class DashboardState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val budgetLimit: Double = 0.0,
    val recentExpenses: List<ExpenseEntity> = emptyList()
)
