package com.fireflow.database.security

import java.io.File
import java.security.SecureRandom

/**
 * Resolves the passphrase used to open the SQLCipher database.
 *
 * - Fresh install: generates a random 32-byte passphrase and persists it in
 *   [store] (Android Keystore); the APK never contains a usable secret.
 * - Upgrade from a legacy install (DB encrypted with the APK-embedded
 *   passphrase): persists the new passphrase first, then rekeys the file, so
 *   an interrupted migration is retried on the next launch.
 */
class DatabasePassphraseProvider(
    private val dbFile: File,
    private val store: DbPassphraseStore,
    private val legacyPassphrase: ByteArray,
    private val rekeyer: DatabaseRekeyer
) {

    fun providePassphrase(): ByteArray {
        val stored = store.read()

        if (stored == null) {
            if (!dbFile.exists()) {
                return generateAndStore()
            }
            val fresh = generateAndStore()
            rekeyer.rekey(dbFile, legacyPassphrase, fresh)
            return fresh
        }

        if (!dbFile.exists() || rekeyer.canOpen(dbFile, stored)) {
            return stored
        }

        // Previous launch persisted the passphrase but did not finish the rekey.
        if (rekeyer.canOpen(dbFile, legacyPassphrase)) {
            rekeyer.rekey(dbFile, legacyPassphrase, stored)
        }
        return stored
    }

    private fun generateAndStore(): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_BYTES).also { secureRandom.nextBytes(it) }
        store.write(passphrase)
        return passphrase
    }

    private companion object {
        const val PASSPHRASE_BYTES = 32
        val secureRandom = SecureRandom()
    }
}
