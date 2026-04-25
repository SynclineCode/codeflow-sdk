package com.codeflow.sdk.analytics

import java.util.UUID

data class AnalyticsEvent(
    val name: String,
    val properties: Map<String, Any?> = emptyMap(),
    val timestampMs: Long = System.currentTimeMillis(),
    val id: String = UUID.randomUUID().toString()
)
