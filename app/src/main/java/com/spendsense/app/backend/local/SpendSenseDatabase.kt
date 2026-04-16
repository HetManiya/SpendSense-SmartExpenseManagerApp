package com.spendsense.app.backend.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.spendsense.app.core.util.SecurityUtils
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
        private const val DB_NAME = "spendsense_db"

        fun getDatabase(context: Context): SpendSenseDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            return synchronized(this) {
                val passphrase = SecurityUtils.getDatabasePassphrase(context)
                val factory = SupportOpenHelperFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpendSenseDatabase::class.java,
                    DB_NAME
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()

                // Crucial: SQLCipher validation
                try {
                    // Try to perform a simple query to verify the key
                    instance.openHelper.writableDatabase
                } catch (e: Exception) {
                    Log.e("SpendSenseDatabase", "Encryption key mismatch or corruption. Recreating database.", e)
                    instance.close()
                    context.deleteDatabase(DB_NAME)
                    // Re-attempt creation (one level of recursion)
                    val recoveredInstance = Room.databaseBuilder(
                        context.applicationContext,
                        SpendSenseDatabase::class.java,
                        DB_NAME
                    )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()
                    
                    INSTANCE = recoveredInstance
                    return@synchronized recoveredInstance
                }

                INSTANCE = instance
                instance
            }
        }
    }
}
