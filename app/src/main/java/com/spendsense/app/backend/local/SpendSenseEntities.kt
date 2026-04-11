package com.spendsense.app.backend.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double = 0.0,
    val category: String = "",
    val date: Long = 0L,
    val note: String = "",
    @ColumnInfo(name = "payment_method") val paymentMethod: String = "",
    val firebaseId: String? = null
)

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double = 0.0,
    val date: Long = 0L,
    val source: String = "",
    val note: String = "",
    val firebaseId: String? = null
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val month: String = "",
    @ColumnInfo(name = "limit_amount") val limitAmount: Double = 0.0
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val email: String = "",
    val currencySymbol: String = "₹",
    val incomeRange: String = "",
    val groupId: String? = null
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val deadline: Long,
    val category: String = "General"
)
