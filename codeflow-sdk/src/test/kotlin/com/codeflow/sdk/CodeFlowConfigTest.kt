package com.codeflow.sdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeFlowConfigTest {

    @Test
    fun `debug logging defaults to off`() {
        val config = CodeFlowConfig(applicationId = "app-123")

        assertEquals("app-123", config.applicationId)
        assertFalse(config.debugLogging)
    }

    @Test
    fun `retains explicitly provided values`() {
        val config = CodeFlowConfig(applicationId = "app-123", debugLogging = true)

        assertEquals("app-123", config.applicationId)
        assertTrue(config.debugLogging)
    }

    @Test
    fun `copy overrides a single field`() {
        val base = CodeFlowConfig(applicationId = "app-123")

        val updated = base.copy(debugLogging = true)

        assertEquals(base.applicationId, updated.applicationId)
        assertTrue(updated.debugLogging)
    }
}
