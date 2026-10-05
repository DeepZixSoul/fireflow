package com.igrupos.app

/**
 * Default seed credentials for first-run initialization.
 * The user is forced to change their password on first login.
 * In production, override via BuildConfig or remote configuration.
 */
internal object SeedCredentials {
    const val ADMIN_PASSWORD: String = "ChangeMe!2026"
}
