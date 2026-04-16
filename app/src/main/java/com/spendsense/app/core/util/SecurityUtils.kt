package com.spendsense.app.core.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File
import java.security.SecureRandom

object SecurityUtils {
    private const val PREFS_NAME = "spendsense_secure_prefs"
    private const val DB_KEY = "db_passphrase"

    fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            deleteSharedPrefs(context, PREFS_NAME)
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    fun getDatabasePassphrase(context: Context): ByteArray {
        val prefs = getEncryptedPrefs(context)
        var passphrase = prefs.getString(DB_KEY, null)
        
        if (passphrase == null) {
            val random = SecureRandom()
            val bytes = ByteArray(32)
            random.nextBytes(bytes)
            passphrase = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
            prefs.edit().putString(DB_KEY, passphrase).apply()
        }

        return android.util.Base64.decode(passphrase, android.util.Base64.DEFAULT)
    }

    private fun deleteSharedPrefs(context: Context, name: String) {
        try {
            context.deleteSharedPreferences(name)
        } catch (e: Exception) {
            val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            val prefsFile = File(sharedPrefsDir, "$name.xml")
            if (prefsFile.exists()) prefsFile.delete()
        }
    }
}
