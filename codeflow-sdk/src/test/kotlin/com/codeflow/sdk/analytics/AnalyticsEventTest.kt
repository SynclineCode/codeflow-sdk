package com.codeflow.sdk.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsEventTest {

    @Test
    fun `applies defaults for properties id and timestamp`() {
        val before = System.currentTimeMillis()
        val event = AnalyticsEvent(name = "screen_view")
        val after = System.currentTimeMillis()

        assertEquals("screen_view", event.name)
        assertTrue(event.properties.isEmpty())
        assertTrue(event.id.isNotBlank())
        assertTrue(event.timestampMs in before..after)
    }

    @Test
    fun `generates a unique id per event`() {
        val a = AnalyticsEvent(name = "tap")
        val b = AnalyticsEvent(name = "tap")

        assertNotEquals(a.id, b.id)
    }

    @Test
    fun `retains supplied properties`() {
        val event = AnalyticsEvent(
            name = "ui_tap",
            properties = mapOf("label" to "Submit", "screen" to "Checkout"),
        )

        assertEquals("Submit", event.properties["label"])
        assertEquals("Checkout", event.properties["screen"])
    }
}
