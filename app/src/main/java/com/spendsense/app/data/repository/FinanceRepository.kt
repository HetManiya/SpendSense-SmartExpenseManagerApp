package com.spendsense.app.data.repository

import com.spendsense.app.data.local.*
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val dao: SpendSenseDao) {

    val allExpenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    val allIncomes: Flow<List<IncomeEntity>> = dao.getAllIncomes()
    val userProfile: Flow<UserEntity?> = dao.getUserProfile()
    val allGoals: Flow<List<GoalEntity>> = dao.getAllGoals()

    suspend fun addExpense(expense: ExpenseEntity) = dao.insertExpense(expense)
    suspend fun addIncome(income: IncomeEntity) = dao.insertIncome(income)
    suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)
    suspend fun deleteIncome(income: IncomeEntity) = dao.deleteIncome(income)

    fun getBudgetForMonth(monthYear: String): Flow<BudgetEntity?> = dao.getBudgetForMonth(monthYear)
    suspend fun setBudget(budget: BudgetEntity) = dao.insertBudget(budget)

    suspend fun updateUserProfile(user: UserEntity) = dao.insertUser(user)

    // Goals
    suspend fun addGoal(goal: GoalEntity) = dao.insertGoal(goal)
    suspend fun deleteGoal(goal: GoalEntity) = dao.deleteGoal(goal)
}
