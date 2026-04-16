package com.spendsense.app.backend.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.spendsense.app.backend.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FinanceRepository @Inject constructor(
    private val dao: SpendSenseDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val userId: String?
        get() = auth.currentUser?.uid

    val allExpenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    val allIncomes: Flow<List<IncomeEntity>> = dao.getAllIncomes()
    val userProfile: Flow<UserEntity?> = dao.getUserProfile()
    val allGoals: Flow<List<GoalEntity>> = dao.getAllGoals()

    suspend fun addExpense(expense: ExpenseEntity) {
        val rowId = dao.insertExpense(expense)
        val updatedExpense = expense.copy(id = rowId.toInt())
        syncExpenseToCloud(updatedExpense)
    }

    suspend fun addIncome(income: IncomeEntity) {
        val rowId = dao.insertIncome(income)
        val updatedIncome = income.copy(id = rowId.toInt())
        syncIncomeToCloud(updatedIncome)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        dao.deleteExpense(expense)
        val uid = userId ?: return
        try {
            firestore.collection("users")
                .document(uid)
                .collection("expenses")
                .document(expense.id.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Error deleting cloud expense", e)
        }
    }

    suspend fun deleteIncome(income: IncomeEntity) {
        dao.deleteIncome(income)
        val uid = userId ?: return
        try {
            firestore.collection("users")
                .document(uid)
                .collection("incomes")
                .document(income.id.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Error deleting cloud income", e)
        }
    }

    fun getBudgetForMonth(monthYear: String): Flow<BudgetEntity?> = dao.getBudgetForMonth(monthYear)
    
    suspend fun setBudget(budget: BudgetEntity) {
        dao.insertBudget(budget)
        syncBudgetToCloud(budget)
    }

    suspend fun updateUserProfile(user: UserEntity) {
        dao.insertUser(user)
        syncProfileToCloud(user)
    }

    // Goals
    suspend fun addGoal(goal: GoalEntity) {
        val rowId = dao.insertGoal(goal)
        syncGoalToCloud(goal.copy(id = rowId.toInt()))
    }
    
    suspend fun deleteGoal(goal: GoalEntity) = dao.deleteGoal(goal)

    // Firestore Sync Methods
    private suspend fun syncExpenseToCloud(expense: ExpenseEntity) {
        val uid = userId ?: return
        try {
            firestore.collection("users")
                .document(uid)
                .collection("expenses")
                .document(expense.id.toString())
                .set(expense, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Error syncing expense", e)
        }
    }

    private suspend fun syncIncomeToCloud(income: IncomeEntity) {
        val uid = userId ?: return
        try {
            firestore.collection("users")
                .document(uid)
                .collection("incomes")
                .document(income.id.toString())
                .set(income, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Error syncing income", e)
        }
    }

    private suspend fun syncBudgetToCloud(budget: BudgetEntity) {
        val uid = userId ?: return
        try {
            firestore.collection("users")
                .document(uid)
                .collection("budgets")
                .document(budget.month)
                .set(budget, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Error syncing budget", e)
        }
    }

    private suspend fun syncProfileToCloud(user: UserEntity) {
        val uid = userId ?: return
        try {
            firestore.collection("users")
                .document(uid)
                .set(user, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Error syncing profile", e)
        }
    }

    private suspend fun syncGoalToCloud(goal: GoalEntity) {
        val uid = userId ?: return
        try {
            firestore.collection("users")
                .document(uid)
                .collection("goals")
                .document(goal.id.toString())
                .set(goal, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Error syncing goal", e)
        }
    }

    suspend fun syncAllLocalDataToCloud() {
        if (userId == null) return
        try {
            val expenses = allExpenses.first()
            val incomes = allIncomes.first()
            val profile = userProfile.first()
            
            profile?.let { syncProfileToCloud(it) }
            expenses.forEach { syncExpenseToCloud(it) }
            incomes.forEach { syncIncomeToCloud(it) }
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Error during full sync", e)
        }
    }

    // Clear all local data on logout
    suspend fun clearAllData() {
        dao.deleteAllExpenses()
        dao.deleteAllIncomes()
        dao.deleteAllBudgets()
        dao.deleteAllGoals()
        dao.deleteUser()
    }
}
