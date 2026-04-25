package com.codeflow.sdk.analytics

data class AnalyticsConfig(
    val applicationId: String,
    val apiUrl: String? = null,
    val debugLogging: Boolean = false,
    val captureTaps: Boolean = true,
    val captureLongPresses: Boolean = true,
    val maxLabelChars: Int = 80,
    val recentBufferSize: Int = 100,
    val flushBatchSize: Int = 100
)
