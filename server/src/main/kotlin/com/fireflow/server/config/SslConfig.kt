package com.fireflow.server.config

import java.io.File
import java.io.FileInputStream
import java.security.KeyStore

/**
 * TLS configuration for the server.
 *
 * Present only when `SSL_KEYSTORE` is set; without it the server runs plain
 * HTTP (development / LAN only).
 */
data class SslConfig(
    val keystorePath: String,
    val keyAlias: String,
    val keystorePassword: String,
    val privateKeyPassword: String
) {
    val keystoreType: String
        get() = when {
            keystorePath.endsWith(".p12", ignoreCase = true) ||
                keystorePath.endsWith(".pfx", ignoreCase = true) -> "PKCS12"

            else -> "JKS"
        }

    fun loadKeyStore(): KeyStore {
        val file = File(keystorePath)
        require(file.exists()) { "SSL keystore not found: $keystorePath" }

        val keyStore = KeyStore.getInstance(keystoreType)
        FileInputStream(file).use { stream ->
            keyStore.load(stream, keystorePassword.toCharArray())
        }
        require(keyStore.containsAlias(keyAlias)) {
            "Key alias '$keyAlias' not found in $keystorePath"
        }
        return keyStore
    }

    companion object {
        fun fromEnvironment(env: (String) -> String? = System::getenv): SslConfig? {
            val path = env("SSL_KEYSTORE")?.takeIf { it.isNotBlank() } ?: return null
            val alias = env("SSL_KEY_ALIAS")?.takeIf { it.isNotBlank() }
                ?: error("SSL_KEY_ALIAS is required when SSL_KEYSTORE is set")
            val keystorePassword = env("SSL_KEYSTORE_PASSWORD")?.takeIf { it.isNotBlank() }
                ?: error("SSL_KEYSTORE_PASSWORD is required when SSL_KEYSTORE is set")
            val privateKeyPassword = env("SSL_PRIVATE_KEY_PASSWORD")?.takeIf { it.isNotBlank() }
                ?: keystorePassword

            return SslConfig(
                keystorePath = path,
                keyAlias = alias,
                keystorePassword = keystorePassword,
                privateKeyPassword = privateKeyPassword
            )
        }
    }
}
