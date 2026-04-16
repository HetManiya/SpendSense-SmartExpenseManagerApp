package com.spendsense.app.core.di

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.spendsense.app.backend.local.SpendSenseDao
import com.spendsense.app.backend.local.SpendSenseDatabase
import com.spendsense.app.backend.repository.FinanceRepository
import com.spendsense.app.backend.repository.SmartInsightsRepository
import com.spendsense.app.core.util.SecurityUtils
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
    fun provideFinanceRepository(
        dao: SpendSenseDao,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): FinanceRepository {
        return FinanceRepository(dao, firestore, auth)
    }

    @Provides
    @Singleton
    fun provideSmartInsightsRepository(): SmartInsightsRepository {
        return SmartInsightsRepository()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return SecurityUtils.getEncryptedPrefs(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}
