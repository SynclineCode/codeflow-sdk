package com.codeflow.sdk.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises the framework-free parts of [CodeFlowAnalytics] — sink fan-out and the
 * recent-event buffer — without calling init() (which wires up Android lifecycle
 * callbacks that need a real runtime).
 */
class CodeFlowAnalyticsTest {

    private class RecordingSink : AnalyticsSink {
        val events = mutableListOf<AnalyticsEvent>()
        override fun track(event: AnalyticsEvent) {
            events.add(event)
        }
    }

    @Test
    fun `track fans out to registered sinks`() {
        val sink = RecordingSink()
        CodeFlowAnalytics.addSink(sink)
        try {
            val event = AnalyticsEvent(name = "purchase", properties = mapOf("sku" to "abc"))
            CodeFlowAnalytics.track(event)

            assertTrue(sink.events.any { it.id == event.id })
        } finally {
            CodeFlowAnalytics.removeSink(sink)
        }
    }

    @Test
    fun `name and properties overload builds an event`() {
        val sink = RecordingSink()
        CodeFlowAnalytics.addSink(sink)
        try {
            CodeFlowAnalytics.track("button_click", mapOf("id" to 7))

            val tracked = sink.events.last()
            assertEquals("button_click", tracked.name)
            assertEquals(7, tracked.properties["id"])
        } finally {
            CodeFlowAnalytics.removeSink(sink)
        }
    }

    @Test
    fun `removed sink no longer receives events`() {
        val sink = RecordingSink()
        CodeFlowAnalytics.addSink(sink)
        CodeFlowAnalytics.removeSink(sink)

        CodeFlowAnalytics.track("ignored")

        assertTrue(sink.events.isEmpty())
    }

    @Test
    fun `recent events buffer retains tracked events`() {
        val event = AnalyticsEvent(name = "screen_view")
        CodeFlowAnalytics.track(event)

        assertTrue(CodeFlowAnalytics.recentEvents().any { it.id == event.id })
    }
}
