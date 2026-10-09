package com.fireflow.server.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SslConfigTest {

    private fun env(vararg pairs: Pair<String, String>): (String) -> String? {
        val map = pairs.toMap()
        return { key -> map[key] }
    }

    @Test
    fun `missing SSL_KEYSTORE disables TLS`() {
        assertNull(SslConfig.fromEnvironment(env()))
    }

    @Test
    fun `blank SSL_KEYSTORE disables TLS`() {
        assertNull(SslConfig.fromEnvironment(env("SSL_KEYSTORE" to "  ")))
    }

    @Test
    fun `complete environment returns TLS config`() {
        val config = SslConfig.fromEnvironment(
            env(
                "SSL_KEYSTORE" to "/app/keystore.jks",
                "SSL_KEY_ALIAS" to "fireflow",
                "SSL_KEYSTORE_PASSWORD" to "store-pass",
                "SSL_PRIVATE_KEY_PASSWORD" to "key-pass"
            )
        )

        assertNotNull(config)
        assertEquals("/app/keystore.jks", config!!.keystorePath)
        assertEquals("fireflow", config.keyAlias)
        assertEquals("store-pass", config.keystorePassword)
        assertEquals("key-pass", config.privateKeyPassword)
        assertEquals("JKS", config.keystoreType)
    }

    @Test
    fun `missing SSL_KEY_ALIAS fails loudly`() {
        assertThrows<IllegalStateException> {
            SslConfig.fromEnvironment(env("SSL_KEYSTORE" to "/app/keystore.jks"))
        }
    }

    @Test
    fun `missing SSL_KEYSTORE_PASSWORD fails loudly`() {
        assertThrows<IllegalStateException> {
            SslConfig.fromEnvironment(
                env(
                    "SSL_KEYSTORE" to "/app/keystore.jks",
                    "SSL_KEY_ALIAS" to "fireflow"
                )
            )
        }
    }

    @Test
    fun `private key password falls back to the keystore password`() {
        val config = SslConfig.fromEnvironment(
            env(
                "SSL_KEYSTORE" to "/app/keystore.jks",
                "SSL_KEY_ALIAS" to "fireflow",
                "SSL_KEYSTORE_PASSWORD" to "store-pass"
            )
        )

        assertEquals("store-pass", config!!.privateKeyPassword)
    }

    @Test
    fun `keystore type is detected from the file extension`() {
        val p12 = SslConfig(
            keystorePath = "/app/keystore.p12",
            keyAlias = "fireflow",
            keystorePassword = "pass",
            privateKeyPassword = "pass"
        )
        val jks = p12.copy(keystorePath = "/app/keystore.jks")

        assertEquals("PKCS12", p12.keystoreType)
        assertEquals("JKS", jks.keystoreType)
    }

    @Test
    fun `loadKeyStore fails when the file does not exist`() {
        val config = SslConfig(
            keystorePath = "/tmp/definitely-missing-keystore.jks",
            keyAlias = "fireflow",
            keystorePassword = "pass",
            privateKeyPassword = "pass"
        )

        assertThrows<IllegalArgumentException> { config.loadKeyStore() }
    }
}
