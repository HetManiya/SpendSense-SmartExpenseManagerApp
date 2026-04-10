package com.spendsense.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import net.zetetic.database.sqlcipher.SQLiteDatabase

class SpendSenseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 1. Initialize SQLCipher native library (Best practice for sqlcipher-android 4.6.1+)
        System.loadLibrary("sqlcipher")
        
        // 2. Initialize Firebase with a safety check
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                // We use manual initialization as a fallback if google-services.json is missing or plugin fails
                // In a production app, you should ensure google-services.json is correctly placed in /app folder
                val options = FirebaseOptions.Builder()
                    .setApplicationId("com.spendsense.app")
                    .setApiKey("dummy_api_key")
                    .setProjectId("spendsense-app")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
