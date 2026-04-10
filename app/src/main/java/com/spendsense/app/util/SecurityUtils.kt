package com.spendsense.app.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

object SecurityUtils {
    private const val PREFS_NAME = "secret_prefs"
    private const val DB_KEY = "db_passphrase"

    fun getDatabasePassphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var passphrase = sharedPreferences.getString(DB_KEY, null)
        if (passphrase == null) {
            val random = SecureRandom()
            val bytes = ByteArray(32)
            random.nextBytes(bytes)
            passphrase = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
            sharedPreferences.edit().putString(DB_KEY, passphrase).apply()
        }

        return android.util.Base64.decode(passphrase, android.util.Base64.DEFAULT)
    }
}
