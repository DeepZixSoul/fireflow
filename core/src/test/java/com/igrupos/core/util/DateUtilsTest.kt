package com.igrupos.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {

    @Test
    fun `formatDate returns dd MM yyyy format`() {
        val timestamp = 1700000000000L
        val result = DateUtils.formatDate(timestamp)
        assertEquals(10, result.length)
    }

    @Test
    fun `formatDateTime includes time`() {
        val timestamp = 1700000000000L
        val result = DateUtils.formatDateTime(timestamp)
        assertEquals(16, result.length)
    }

    @Test
    fun `formatIso returns ISO string`() {
        val timestamp = 1700000000000L
        val result = DateUtils.formatIso(timestamp)
        assertNotNull(result)
        assertTrue(result.contains("T"))
    }

    @Test
    fun `parseIso with valid ISO string returns timestamp`() {
        val timestamp = 1700000000000L
        val isoString = DateUtils.formatIso(timestamp)
        val parsed = DateUtils.parseIso(isoString)
        assertNotNull(parsed)
    }

    @Test
    fun `parseIso with invalid string returns null`() {
        val result = DateUtils.parseIso("not-a-date")
        assertNull(result)
    }

    @Test
    fun `formatDate with epoch zero`() {
        val result = DateUtils.formatDate(0L)
        assertNotNull(result)
        assertEquals(10, result.length)
    }
}
