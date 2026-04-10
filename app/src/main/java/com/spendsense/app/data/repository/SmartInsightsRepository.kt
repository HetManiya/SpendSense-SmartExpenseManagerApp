package com.spendsense.app.data.repository

import com.spendsense.app.data.local.ExpenseEntity

class SmartInsightsRepository {

    // Smart Auto-Categorization logic
    fun predictCategory(note: String): String {
        val lowerNote = note.lowercase()
        return when {
            lowerNote.contains("food") || lowerNote.contains("dinner") || lowerNote.contains("lunch") || 
            lowerNote.contains("burger") || lowerNote.contains("pizza") || lowerNote.contains("restaurant") || 
            lowerNote.contains("cafe") || lowerNote.contains("starbucks") -> "Food"
            
            lowerNote.contains("uber") || lowerNote.contains("taxi") || lowerNote.contains("flight") || 
            lowerNote.contains("bus") || lowerNote.contains("train") || lowerNote.contains("fuel") || 
            lowerNote.contains("gas") || lowerNote.contains("metro") -> "Travel"
            
            lowerNote.contains("rent") || lowerNote.contains("lease") || lowerNote.contains("apartment") -> "Rent"
            
            lowerNote.contains("hospital") || lowerNote.contains("medicine") || lowerNote.contains("doctor") || 
            lowerNote.contains("pharmacy") || lowerNote.contains("health") -> "Health"
            
            lowerNote.contains("amazon") || lowerNote.contains("zara") || lowerNote.contains("h&m") || 
            lowerNote.contains("clothes") || lowerNote.contains("shopping") || lowerNote.contains("mall") -> "Shopping"
            
            lowerNote.contains("netflix") || lowerNote.contains("spotify") || lowerNote.contains("movie") || 
            lowerNote.contains("game") || lowerNote.contains("disney") -> "Entertainment"
            
            else -> "Others"
        }
    }

    // Mocking Gemini API for 3 actionable money-saving tips
    suspend fun getThreeActionableTips(expenses: List<ExpenseEntity>): List<String> {
        if (expenses.isEmpty()) return listOf(
            "Begin logging your transactions to unlock AI-driven financial insights.",
            "Establish a monthly budget limit in settings to monitor system health.",
            "Categorize your spending to help the AI identify leakage points."
        )

        val categories = expenses.groupBy { it.category }
        val topCategory = categories.maxByOrNull { it.value.sumOf { e -> e.amount } }?.key ?: "N/A"
        
        return listOf(
            "CRITICAL: Your spending in '$topCategory' is 15% higher than last month. Consider tactical reductions.",
            "OPTIMIZATION: Switch to 'Cash' for minor transactions to reduce digital trace and impulsive spending.",
            "INSIGHT: 40% of your expenses occur on weekends. Deploy a 'No-Spend Saturday' protocol to boost balance."
        )
    }

    fun suggestBudgetGoal(historicalExpenses: List<ExpenseEntity>): Double {
        if (historicalExpenses.isEmpty()) return 1000.0
        val total = historicalExpenses.sumOf { it.amount }
        return (total / 30.0) * 0.9 // Suggesting 10% less than current daily avg
    }
}
