package com.spendsense.app.frontend.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.spendsense.app.backend.local.BudgetEntity
import com.spendsense.app.backend.local.ExpenseEntity
import com.spendsense.app.backend.local.IncomeEntity
import com.spendsense.app.backend.repository.FinanceRepository
import com.spendsense.app.backend.repository.SmartInsightsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val smartInsights: SmartInsightsRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private var firestoreListener: ListenerRegistration? = null
    private val _groupId = MutableStateFlow<String?>(null)

    val allExpenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allIncomes = repository.allIncomes.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _currentMonthYear = MutableStateFlow(SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()))
    val currentMonthYear: StateFlow<String> = _currentMonthYear

    val dashboardState = combine(allExpenses, allIncomes, _currentMonthYear) { expenses, incomes, month ->
        val currentMonthExpenses = expenses.filter { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.date)) == month }
        val totalExpense = currentMonthExpenses.sumOf { it.amount }
        val totalIncome = incomes.filter { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.date)) == month }.sumOf { it.amount }
        val balance = totalIncome - totalExpense
        
        // This is a bit tricky since getBudgetForMonth is a Flow. 
        // For simplicity in this split, we'll keep it as is or improve the repository.
        DashboardState(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance,
            recentExpenses = expenses.take(10)
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, DashboardState())

    fun refreshCurrentMonth() {
        _currentMonthYear.value = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    fun addOrUpdateExpense(id: Int = 0, amount: Double, category: String, note: String, paymentMethod: String, isGuest: Boolean) {
        viewModelScope.launch {
            val finalCategory = if(category == "Auto" || category.isEmpty()) smartInsights.predictCategory(note) else category
            val expense = ExpenseEntity(
                id = id,
                amount = amount,
                category = finalCategory,
                date = System.currentTimeMillis(),
                note = note,
                paymentMethod = paymentMethod
            )
            repository.addExpense(expense)
            if (!isGuest) syncExpenseToFirebase(expense)
        }
    }

    private fun syncExpenseToFirebase(expense: ExpenseEntity) {
        val targetId = _groupId.value ?: auth.currentUser?.uid ?: return
        val collectionPath = if (_groupId.value != null) "groups" else "users"
        firestore.collection(collectionPath).document(targetId)
            .collection("expenses").document(expense.id.toString()).set(expense)
    }

    fun startSync(groupId: String?) {
        _groupId.value = groupId
        val targetId = groupId ?: auth.currentUser?.uid ?: return
        val collectionPath = if (groupId != null) "groups" else "users"
        
        firestoreListener?.remove()
        firestoreListener = firestore.collection(collectionPath).document(targetId)
            .collection("expenses")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                viewModelScope.launch {
                    snapshots?.documents?.forEach { doc ->
                        val expense = doc.toObject(ExpenseEntity::class.java)
                        expense?.let { repository.addExpense(it) }
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        firestoreListener?.remove()
    }
}
