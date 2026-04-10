package com.spendsense.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.spendsense.app.data.local.*
import com.spendsense.app.data.repository.FinanceRepository
import com.spendsense.app.data.repository.SmartInsightsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val repository: FinanceRepository,
    private val smartInsights: SmartInsightsRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _isGuest = MutableStateFlow(false)
    val isGuest: StateFlow<Boolean> = _isGuest

    private val _groupId = MutableStateFlow<String?>(null)
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
                if (_groupId.value != null) {
                    startFirestoreSync(_groupId.value!!)
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

    fun setAuthenticated(value: Boolean) {
        _isAuthenticated.value = value
        _isGuest.value = false
    }

    fun setGuestMode(value: Boolean) {
        _isGuest.value = value
        _isAuthenticated.value = false
    }

    fun logout() {
        auth.signOut()
        _isAuthenticated.value = false
        _isGuest.value = false
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
            .collection("expenses").add(expense)
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
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
        val collectionPath = if (_groupId.value != null) "groups" else "users"
        firestore.collection(collectionPath).document(targetId)
            .collection("incomes").add(income)
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
        // Shared mode listener
    }
}

data class DashboardState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val budgetLimit: Double = 0.0,
    val recentExpenses: List<ExpenseEntity> = emptyList()
)

class MainViewModelFactory(
    private val repository: FinanceRepository,
    private val smartInsights: SmartInsightsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, smartInsights) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
