package com.spendsense.app.frontend.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentChange
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
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    private var expensesListener: ListenerRegistration? = null
    private var incomesListener: ListenerRegistration? = null

    private val _isGuest = MutableStateFlow(sharedPrefs.getBoolean("is_guest", false))
    val isGuest: StateFlow<Boolean> = _isGuest

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    private val _firebaseUser = MutableStateFlow(auth.currentUser)

    val isAuthenticated = combine(_isGuest, _firebaseUser) { guest, user ->
        guest || user != null
    }.stateIn(viewModelScope, SharingStarted.Lazily, sharedPrefs.getBoolean("is_guest", false) || auth.currentUser != null)

    private val _groupId = MutableStateFlow<String?>(sharedPrefs.getString("group_id", null))
    val groupId: StateFlow<String?> = _groupId

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
        auth.addAuthStateListener { firebaseAuth ->
            _firebaseUser.value = firebaseAuth.currentUser
            firebaseAuth.currentUser?.uid?.let { uid -> loadUserGroup(uid) }
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
                val gId = doc.getString("groupId")
                _groupId.value = gId
                sharedPrefs.edit().putString("group_id", gId).apply()
                startFirestoreSync(gId, userId)
            }
            .addOnFailureListener {
                // If user doc doesn't exist, still sync personal data
                startFirestoreSync(null, userId)
            }
    }

    fun setGuestMode(value: Boolean) {
        _isGuest.value = value
        sharedPrefs.edit().putBoolean("is_guest", value).apply()
        if (value) {
            auth.signOut()
            stopFirestoreSync()
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                setGuestMode(false)
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Authentication failed"
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
                auth.createUserWithEmailAndPassword(email, pass).await()
                setGuestMode(false)
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Signup failed"
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
                auth.signInWithCredential(credential).await()
                setGuestMode(false)
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Google Login failed"
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
                    sharedPrefs.edit().putString("group_id", newGroupId).apply()
                    startFirestoreSync(newGroupId, userId)
                }
        }
    }

    fun joinGroup(id: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestore.collection("users").document(userId).update("groupId", id)
                .addOnSuccessListener {
                    _groupId.value = id
                    sharedPrefs.edit().putString("group_id", id).apply()
                    startFirestoreSync(id, userId)
                }
        }
    }

    fun leaveGroup() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestore.collection("users").document(userId).update("groupId", null)
                .addOnSuccessListener {
                    _groupId.value = null
                    sharedPrefs.edit().remove("group_id").apply()
                    startFirestoreSync(null, userId)
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            repository.clearAllData()
            sharedPrefs.edit()
                .putBoolean("is_guest", false)
                .remove("group_id")
                .apply()
            _isGuest.value = false
            _groupId.value = null
            stopFirestoreSync()
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
            
            // Generate a unique firebaseId if it's a new entry
            val existingExpense = allExpenses.value.find { it.id == id }
            val fId = existingExpense?.firebaseId ?: UUID.randomUUID().toString()
            
            val expense = ExpenseEntity(
                id = id,
                amount = amount,
                category = finalCategory,
                date = date ?: System.currentTimeMillis(),
                note = note,
                paymentMethod = paymentMethod,
                firebaseId = fId
            )
            repository.addExpense(expense)
            if (!_isGuest.value) syncExpenseToFirebase(expense)
        }
    }

    private fun syncExpenseToFirebase(expense: ExpenseEntity) {
        val userId = auth.currentUser?.uid ?: return
        val targetId = _groupId.value ?: userId
        val collectionPath = if (_groupId.value != null) "groups" else "users"
        
        firestore.collection(collectionPath).document(targetId)
            .collection("expenses").document(expense.firebaseId ?: expense.id.toString()).set(expense)
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            if (!_isGuest.value) {
                val userId = auth.currentUser?.uid ?: return@launch
                val targetId = _groupId.value ?: userId
                val collectionPath = if (_groupId.value != null) "groups" else "users"
                firestore.collection(collectionPath).document(targetId)
                    .collection("expenses").document(expense.firebaseId ?: expense.id.toString()).delete()
            }
        }
    }

    fun addOrUpdateIncome(id: Int = 0, amount: Double, source: String, note: String, date: Long? = null) {
        viewModelScope.launch {
            val existingIncome = allIncomes.value.find { it.id == id }
            val fId = existingIncome?.firebaseId ?: UUID.randomUUID().toString()

            val income = IncomeEntity(
                id = id,
                amount = amount,
                date = date ?: System.currentTimeMillis(),
                source = source,
                note = note,
                firebaseId = fId
            )
            repository.addIncome(income)
            if (!_isGuest.value) syncIncomeToFirebase(income)
        }
    }

    private fun syncIncomeToFirebase(income: IncomeEntity) {
        val userId = auth.currentUser?.uid ?: return
        val targetId = _groupId.value ?: userId
        val collectionPath = if (_groupId.value != null) "groups" else "users"
        
        firestore.collection(collectionPath).document(targetId)
            .collection("incomes").document(income.firebaseId ?: income.id.toString()).set(income)
    }

    fun deleteIncome(income: IncomeEntity) {
        viewModelScope.launch {
            repository.deleteIncome(income)
            if (!_isGuest.value) {
                val userId = auth.currentUser?.uid ?: return@launch
                val targetId = _groupId.value ?: userId
                val collectionPath = if (_groupId.value != null) "groups" else "users"
                firestore.collection(collectionPath).document(targetId)
                    .collection("incomes").document(income.firebaseId ?: income.id.toString()).delete()
            }
        }
    }

    fun saveUserProfile(name: String, currency: String, budgetLimit: Double, incomeRange: String = "") {
        viewModelScope.launch {
            repository.updateUserProfile(UserEntity(id = 1, name = name, currencySymbol = currency, incomeRange = incomeRange))
            repository.setBudget(BudgetEntity(month = currentMonthYear, limitAmount = budgetLimit))
            
            if (!_isGuest.value) {
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

    private fun startFirestoreSync(groupId: String?, userId: String) {
        stopFirestoreSync()
        
        val targetId = groupId ?: userId
        val collectionPath = if (groupId != null) "groups" else "users"

        // Expenses Sync
        expensesListener = firestore.collection(collectionPath).document(targetId)
            .collection("expenses")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("MainViewModel", "Expenses listener failed", e)
                    return@addSnapshotListener
                }
                
                viewModelScope.launch {
                    snapshots?.documentChanges?.forEach { dc ->
                        val expense = dc.document.toObject(ExpenseEntity::class.java)
                        when (dc.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                val existing = allExpenses.value.find { it.firebaseId == expense.firebaseId }
                                repository.addExpense(expense.copy(id = existing?.id ?: 0))
                            }
                            DocumentChange.Type.REMOVED -> {
                                allExpenses.value.find { it.firebaseId == expense.firebaseId }?.let {
                                    repository.deleteExpense(it)
                                }
                            }
                        }
                    }
                }
            }

        // Incomes Sync
        incomesListener = firestore.collection(collectionPath).document(targetId)
            .collection("incomes")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("MainViewModel", "Incomes listener failed", e)
                    return@addSnapshotListener
                }

                viewModelScope.launch {
                    snapshots?.documentChanges?.forEach { dc ->
                        val income = dc.document.toObject(IncomeEntity::class.java)
                        when (dc.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                val existing = allIncomes.value.find { it.firebaseId == income.firebaseId }
                                repository.addIncome(income.copy(id = existing?.id ?: 0))
                            }
                            DocumentChange.Type.REMOVED -> {
                                allIncomes.value.find { it.firebaseId == income.firebaseId }?.let {
                                    repository.deleteIncome(it)
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun stopFirestoreSync() {
        expensesListener?.remove()
        incomesListener?.remove()
        expensesListener = null
        incomesListener = null
    }

    override fun onCleared() {
        super.onCleared()
        stopFirestoreSync()
    }
}

data class DashboardState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val budgetLimit: Double = 0.0,
    val recentExpenses: List<ExpenseEntity> = emptyList()
)
