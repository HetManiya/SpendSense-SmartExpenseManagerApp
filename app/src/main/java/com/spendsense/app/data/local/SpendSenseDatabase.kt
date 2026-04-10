package com.spendsense.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.spendsense.app.util.SecurityUtils
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        ExpenseEntity::class, 
        IncomeEntity::class, 
        BudgetEntity::class, 
        UserEntity::class,
        GoalEntity::class
    ], 
    version = 2,
    exportSchema = false
)
abstract class SpendSenseDatabase : RoomDatabase() {
    abstract fun spendSenseDao(): SpendSenseDao

    companion object {
        @Volatile
        private var INSTANCE: SpendSenseDatabase? = null

        fun getDatabase(context: Context): SpendSenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = SecurityUtils.getDatabasePassphrase(context)
                // The new sqlcipher-android (4.6.1+) uses SupportOpenHelperFactory from net.zetetic.database.sqlcipher
                val factory = SupportOpenHelperFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpendSenseDatabase::class.java,
                    "spendsense_db"
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
