package com.codeflow.sdk.analytics

import android.app.Application
import android.util.Log
import com.codeflow.sdk.BuildConfig
import com.codeflow.sdk.analytics.internal.ActivityTracker
import com.codeflow.sdk.analytics.internal.AnalyticsUploader
import java.util.ArrayDeque
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

object CodeFlowAnalytics {

    private const val TAG = "CodeFlowAnalytics"

    private val initialized = AtomicBoolean(false)
    private val sinks = CopyOnWriteArrayList<AnalyticsSink>()
    private val recent = ArrayDeque<AnalyticsEvent>()

    @Volatile private var _config: AnalyticsConfig? = null
    @Volatile private var tracker: ActivityTracker? = null
    @Volatile private var uploader: AnalyticsUploader? = null

    fun init(application: Application, config: AnalyticsConfig) {
        if (!initialized.compareAndSet(false, true)) return
        _config = config

        val apiUrl = config.apiUrl ?: BuildConfig.ANALYTICS_API_URL
        val u = AnalyticsUploader(
            applicationId = config.applicationId,
            apiUrlBase = apiUrl,
            batchSize = config.flushBatchSize,
            debugLogging = config.debugLogging,
        )
        uploader = u
        sinks.add(UploaderSink(u))
        if (config.debugLogging) sinks.add(LogcatSink)

        val t = ActivityTracker()
        application.registerActivityLifecycleCallbacks(t)
        tracker = t
        Log.i(TAG, "CodeFlowAnalytics initialized → $apiUrl/${config.applicationId}")
    }

    fun addSink(sink: AnalyticsSink) {
        sinks.addIfAbsent(sink)
    }

    fun removeSink(sink: AnalyticsSink) {
        sinks.remove(sink)
    }

    fun track(event: AnalyticsEvent) {
        synchronized(recent) {
            recent.addFirst(event)
            val cap = _config?.recentBufferSize ?: 100
            while (recent.size > cap) recent.removeLast()
        }
        for (sink in sinks) {
            try {
                sink.track(event)
            } catch (t: Throwable) {
                Log.w(TAG, "sink threw: ${t.message}")
            }
        }
    }

    fun track(name: String, properties: Map<String, Any?> = emptyMap()) {
        track(AnalyticsEvent(name = name, properties = properties))
    }

    fun flush() {
        uploader?.flush()
    }

    fun recentEvents(): List<AnalyticsEvent> = synchronized(recent) { recent.toList() }

    internal fun config(): AnalyticsConfig? = _config

    private class UploaderSink(private val uploader: AnalyticsUploader) : AnalyticsSink {
        override fun track(event: AnalyticsEvent) = uploader.enqueue(event)
    }
}
