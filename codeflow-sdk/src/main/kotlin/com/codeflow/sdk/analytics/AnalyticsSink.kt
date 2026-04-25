package com.codeflow.sdk.analytics

import android.util.Log

interface AnalyticsSink {
    fun track(event: AnalyticsEvent)
}

internal object LogcatSink : AnalyticsSink {
    override fun track(event: AnalyticsEvent) {
        Log.i("CodeFlowAnalytics", "[${event.name}] ${event.properties}")
    }
}
