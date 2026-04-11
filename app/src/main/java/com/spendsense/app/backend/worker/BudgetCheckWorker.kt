package com.spendsense.app.backend.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spendsense.app.backend.repository.FinanceRepository
import com.spendsense.app.core.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class BudgetCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: FinanceRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val currentMonthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val budget = repository.getBudgetForMonth(currentMonthYear).firstOrNull() ?: return Result.success()
        val expenses = repository.allExpenses.firstOrNull()?.filter { 
            SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.date)) == currentMonthYear 
        } ?: return Result.success()

        val totalExpense = expenses.sumOf { it.amount }
        val limit = budget.limitAmount

        if (limit > 0) {
            val percentage = (totalExpense / limit) * 100
            when {
                percentage >= 100 -> {
                    NotificationHelper.showBudgetAlert(
                        applicationContext,
                        "Budget Exceeded!",
                        "You have spent ₹${String.format("%.2f", totalExpense)}, which is over your ₹${String.format("%.2f", limit)} limit."
                    )
                }
                percentage >= 80 -> {
                    NotificationHelper.showBudgetAlert(
                        applicationContext,
                        "Budget Warning",
                        "You have used ${percentage.toInt()}% of your monthly budget."
                    )
                }
            }
        }

        return Result.success()
    }
}
