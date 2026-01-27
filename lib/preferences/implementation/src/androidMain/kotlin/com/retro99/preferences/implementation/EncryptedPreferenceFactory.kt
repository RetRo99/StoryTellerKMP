package com.retro99.preferences.implementation

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import java.security.KeyStore

/**
 * Factory for creating encrypted SharedPreferences using AndroidX Security.
 *
 * Handles the AEADBadTagException that can occur when:
 * - The app data is restored from backup but the encryption keys are not
 * - The Android Keystore becomes corrupted
 * - There's a mismatch between the MasterKey and the encrypted keyset
 *
 * In these cases, the corrupted preferences are cleared and recreated.
 */
class EncryptedPreferenceFactory(
    private val context: Context,
) : Settings.Factory {

    override fun create(name: String?): Settings {
        checkNotNull(name) { "Settings Name cannot be null" }

        val sharedPreferences = try {
            createEncryptedSharedPreferences(name)
        } catch (e: Exception) {
            // Handle AEADBadTagException and other security exceptions
            // This can happen when encryption keys don't match the stored data
            // (e.g., after backup restore, factory reset, or Keystore corruption)
            if (isEncryptionException(e)) {
                Log.w(TAG, "Failed to create encrypted preferences, clearing and retrying", e)
                clearCorruptedPreferences(name)
                createEncryptedSharedPreferences(name)
            } else {
                throw e
            }
        }

        return SharedPreferencesSettings(sharedPreferences)
    }

    private fun createEncryptedSharedPreferences(name: String): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            name,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Checks if the exception is related to encryption/decryption failures.
     * This includes AEADBadTagException and other security-related exceptions
     * that can occur when the keyset is corrupted or mismatched.
     */
    private fun isEncryptionException(e: Exception): Boolean {
        var cause: Throwable? = e
        while (cause != null) {
            val message = cause.message ?: ""
            if (cause::class.java.name.contains("AEADBadTagException") ||
                cause::class.java.name.contains("InvalidProtocolBufferException") ||
                cause::class.java.name.contains("GeneralSecurityException") ||
                message.contains("keyset not found") ||
                message.contains("Signature/MAC verification failed")
            ) {
                return true
            }
            cause = cause.cause
        }
        return false
    }

    /**
     * Clears corrupted encrypted preferences by:
     * 1. Deleting the SharedPreferences file
     * 2. Deleting the encrypted keyset stored in a separate SharedPreferences
     * 3. Optionally removing the MasterKey from the Keystore
     */
    private fun clearCorruptedPreferences(name: String) {
        try {
            // Clear the main encrypted SharedPreferences
            context.getSharedPreferences(name, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()

            // Delete the SharedPreferences file
            deleteSharedPreferencesFile(name)

            // The encrypted keyset is stored in a separate SharedPreferences file
            // with the naming convention: __androidx_security_crypto_encrypted_prefs_key_keyset__
            // and __androidx_security_crypto_encrypted_prefs_value_keyset__
            // These are stored within the same SharedPreferences file, so clearing it should suffice

            // Optionally, remove the MasterKey from the Keystore if it's corrupted
            // This is usually not necessary, but can help in some edge cases
            deleteMasterKeyIfExists()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing corrupted preferences", e)
        }
    }

    private fun deleteSharedPreferencesFile(name: String) {
        try {
            val prefsDir = context.dataDir.resolve("shared_prefs")
            val prefsFile = prefsDir.resolve("$name.xml")
            if (prefsFile.exists()) {
                prefsFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting SharedPreferences file", e)
        }
    }

    private fun deleteMasterKeyIfExists() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (keyStore.containsAlias(MasterKey.DEFAULT_MASTER_KEY_ALIAS)) {
                keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting MasterKey from Keystore", e)
        }
    }

    private companion object {
        private const val TAG = "EncryptedPreferenceFactory"
    }
}