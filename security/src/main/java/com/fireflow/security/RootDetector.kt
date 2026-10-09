package com.fireflow.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class RootDetectionResult(
    val isRooted: Boolean,
    val reasons: List<String>
)

/**
 * Detects a rooted device. The result is advisory only: the app shows a
 * warning and keeps working (root detection is not used to block access).
 */
@Singleton
class RootDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun detect(): RootDetectionResult = evaluate(
        buildTags = Build.TAGS,
        presentSuPaths = SU_PATHS.filter { File(it).exists() },
        installedRootApps = ROOT_APPS.filter(::isPackageInstalled)
    )

    internal fun evaluate(
        buildTags: String?,
        presentSuPaths: List<String>,
        installedRootApps: List<String>
    ): RootDetectionResult {
        val reasons = buildList {
            if (!buildTags.isNullOrBlank() && buildTags.contains(TEST_KEYS)) {
                add("Build tags de depuración (test-keys)")
            }
            presentSuPaths.forEach { add("Binario su presente: $it") }
            installedRootApps.forEach { add("App de root instalada: $it") }
        }
        return RootDetectionResult(isRooted = reasons.isNotEmpty(), reasons = reasons)
    }

    private fun isPackageInstalled(packageName: String): Boolean = try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    private companion object {
        const val TEST_KEYS = "test-keys"

        val SU_PATHS = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/data/local/bin/su",
            "/data/local/xbin/su",
            "/su/bin/su"
        )

        val ROOT_APPS = listOf(
            "com.topjohnwu.magisk",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.noshufou.android.su",
            "com.geohot.towelroot"
        )
    }
}
