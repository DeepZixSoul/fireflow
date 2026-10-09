package com.fireflow.database.security

import java.io.File

/**
 * Probes and rotates the SQLCipher key of an existing database file.
 * Kept behind an interface so the migration logic can be unit tested
 * without the native SQLCipher library.
 */
interface DatabaseRekeyer {
    fun canOpen(dbFile: File, passphrase: ByteArray): Boolean

    /** Opens the database with [oldPassphrase] and rotates it to [newPassphrase]. */
    fun rekey(dbFile: File, oldPassphrase: ByteArray, newPassphrase: ByteArray)
}
