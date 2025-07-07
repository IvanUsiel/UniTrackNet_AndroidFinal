package com.irjarqui.unitracknetv3.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

@Suppress("DEPRECATION")
object SecurePrefs {

    private const val PREF_NAME = "secure_prefs"
    private const val KEY_USER = "user"
    private const val KEY_PASSWORD = "password"
    private const val KEY_REMEMBER = "remember_me"

    private fun getPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun saveCredentials(context: Context, user: String, password: String) {
        val prefs = getPrefs(context)
        prefs.edit().apply {
            putString(KEY_USER, user)
            putString(KEY_PASSWORD, password)
            putBoolean(KEY_REMEMBER, true)
            apply()
        }
    }

    fun clearCredentials(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit() { clear() }
    }

    fun isRememberMeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_REMEMBER, false)
    }

    fun getUser(context: Context): String? {
        return getPrefs(context).getString(KEY_USER, null)
    }

    fun getPassword(context: Context): String? {
        return getPrefs(context).getString(KEY_PASSWORD, null)
    }
}
