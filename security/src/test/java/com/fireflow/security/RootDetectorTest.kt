package com.fireflow.security

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RootDetectorTest {

    private val context: android.content.Context = mockk(relaxed = true)
    private val detector = RootDetector(context)

    @Test
    fun `clean device is not detected as rooted`() {
        val result = detector.evaluate(
            buildTags = "release-keys",
            presentSuPaths = emptyList(),
            installedRootApps = emptyList()
        )

        assertFalse(result.isRooted)
        assertTrue(result.reasons.isEmpty())
    }

    @Test
    fun `null build tags are not flagged`() {
        val result = detector.evaluate(
            buildTags = null,
            presentSuPaths = emptyList(),
            installedRootApps = emptyList()
        )

        assertFalse(result.isRooted)
    }

    @Test
    fun `test-keys build tags are flagged`() {
        val result = detector.evaluate(
            buildTags = "test-keys",
            presentSuPaths = emptyList(),
            installedRootApps = emptyList()
        )

        assertTrue(result.isRooted)
        assertEquals(1, result.reasons.size)
        assertTrue(result.reasons[0].contains("test-keys"))
    }

    @Test
    fun `present su binaries are flagged`() {
        val result = detector.evaluate(
            buildTags = "release-keys",
            presentSuPaths = listOf("/system/xbin/su", "/sbin/su"),
            installedRootApps = emptyList()
        )

        assertTrue(result.isRooted)
        assertEquals(2, result.reasons.size)
        assertTrue(result.reasons[0].contains("/system/xbin/su"))
    }

    @Test
    fun `installed root apps are flagged`() {
        val result = detector.evaluate(
            buildTags = "release-keys",
            presentSuPaths = emptyList(),
            installedRootApps = listOf("com.topjohnwu.magisk")
        )

        assertTrue(result.isRooted)
        assertTrue(result.reasons[0].contains("com.topjohnwu.magisk"))
    }

    @Test
    fun `every reason is accumulated`() {
        val result = detector.evaluate(
            buildTags = "test-keys",
            presentSuPaths = listOf("/system/bin/su"),
            installedRootApps = listOf("eu.chainfire.supersu")
        )

        assertTrue(result.isRooted)
        assertEquals(3, result.reasons.size)
    }
}
