package com.spendsense.app.core.di

import android.content.Context
import com.spendsense.app.backend.local.SpendSenseDao
import com.spendsense.app.backend.local.SpendSenseDatabase
import com.spendsense.app.backend.repository.FinanceRepository
import com.spendsense.app.backend.repository.SmartInsightsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SpendSenseDatabase {
        return SpendSenseDatabase.getDatabase(context)
    }

    @Provides
    fun provideSpendSenseDao(database: SpendSenseDatabase): SpendSenseDao {
        return database.spendSenseDao()
    }

    @Provides
    @Singleton
    fun provideFinanceRepository(dao: SpendSenseDao): FinanceRepository {
        return FinanceRepository(dao)
    }

    @Provides
    @Singleton
    fun provideSmartInsightsRepository(): SmartInsightsRepository {
        return SmartInsightsRepository()
    }
}
