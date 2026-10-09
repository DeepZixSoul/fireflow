package com.fireflow.database.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores the passphrase in [EncryptedSharedPreferences] protected by an
 * Android Keystore master key (same pattern as [com.fireflow.security.SessionManager]).
 */
class EncryptedDbPassphraseStore(context: Context) : DbPassphraseStore {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .setUserAuthenticationRequired(false)
        .build()

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun read(): ByteArray? {
        val encoded = prefs.getString(KEY_PASSPHRASE, null) ?: return null
        return Base64.decode(encoded, Base64.NO_WRAP)
    }

    override fun write(passphrase: ByteArray) {
        val encoded = Base64.encodeToString(passphrase, Base64.NO_WRAP)
        prefs.edit().putString(KEY_PASSPHRASE, encoded).commit()
    }

    private companion object {
        const val FILE_NAME = "fireflow_db_passphrase"
        const val KEY_PASSPHRASE = "passphrase"
    }
}
