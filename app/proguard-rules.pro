# SQLCipher specific rules
-keep class net.zetetic.** { *; }
-keep class androidx.sqlite.db.** { *; }

# Firebase specific rules (usually handled by the plugin, but added for safety)
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
