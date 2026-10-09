package com.fireflow.database.security

/**
 * Persists the database passphrase outside the APK.
 *
 * The passphrase is generated at runtime and encrypted with an Android
 * Keystore key, so it is never shipped inside the binary (R2).
 */
interface DbPassphraseStore {
    fun read(): ByteArray?

    /**
     * Must be durable before returning: the passphrase is written before the
     * database is rekeyed so an interrupted migration can be retried.
     */
    fun write(passphrase: ByteArray)
}
