package com.codeflow.sdk.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsConfigTest {

    @Test
    fun `applies sensible defaults`() {
        val config = AnalyticsConfig(applicationId = "app-123")

        assertEquals("app-123", config.applicationId)
        assertNull(config.apiUrl)
        assertFalse(config.debugLogging)
        assertTrue(config.captureTaps)
        assertTrue(config.captureLongPresses)
        assertEquals(80, config.maxLabelChars)
        assertEquals(100, config.recentBufferSize)
        assertEquals(100, config.flushBatchSize)
    }

    @Test
    fun `retains explicitly provided values`() {
        val config = AnalyticsConfig(
            applicationId = "app-123",
            apiUrl = "https://example.test/api",
            debugLogging = true,
            captureTaps = false,
            captureLongPresses = false,
            maxLabelChars = 40,
            recentBufferSize = 10,
            flushBatchSize = 5,
        )

        assertEquals("https://example.test/api", config.apiUrl)
        assertTrue(config.debugLogging)
        assertFalse(config.captureTaps)
        assertFalse(config.captureLongPresses)
        assertEquals(40, config.maxLabelChars)
        assertEquals(10, config.recentBufferSize)
        assertEquals(5, config.flushBatchSize)
    }
}
