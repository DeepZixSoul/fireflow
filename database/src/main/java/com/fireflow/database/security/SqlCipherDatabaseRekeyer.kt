package com.fireflow.database.security

import android.database.sqlite.SQLiteException
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File

/** [DatabaseRekeyer] backed by the native SQLCipher library. */
class SqlCipherDatabaseRekeyer : DatabaseRekeyer {

    init {
        System.loadLibrary("sqlcipher")
    }

    override fun canOpen(dbFile: File, passphrase: ByteArray): Boolean {
        var db: SQLiteDatabase? = null
        return try {
            db = open(dbFile, passphrase, SQLiteDatabase.OPEN_READONLY)
            true
        } catch (e: SQLiteException) {
            false
        } finally {
            db?.close()
        }
    }

    override fun rekey(dbFile: File, oldPassphrase: ByteArray, newPassphrase: ByteArray) {
        val db = open(dbFile, oldPassphrase, SQLiteDatabase.OPEN_READWRITE)
        try {
            db.changePassword(newPassphrase)
        } finally {
            db.close()
        }
    }

    private fun open(dbFile: File, passphrase: ByteArray, flags: Int): SQLiteDatabase =
        SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            passphrase,
            null,
            flags,
            null,
            null
        )
}
