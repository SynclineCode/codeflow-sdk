package com.codeflow.sdk.analytics.internal

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.codeflow.sdk.analytics.AnalyticsEvent
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Batches AnalyticsEvents and POSTs them to the CodeFlow analytics API.
 *
 * Mirrors CodeFlowLogger's batching/flush/lifecycle pattern — events go into
 * a CopyOnWriteArrayList, get flushed on lifecycle pauses or when the buffer
 * hits batchSize, and failed batches are re-queued.
 */
internal class AnalyticsUploader(
    applicationId: String,
    apiUrlBase: String,
    private val batchSize: Int,
    private val debugLogging: Boolean,
) : DefaultLifecycleObserver {

    private val endpoint: String =
        "${apiUrlBase.trimEnd('/')}/${applicationId}"

    private val buffer = CopyOnWriteArrayList<JSONObject>()
    private val executor = Executors.newSingleThreadExecutor()
    private val isFlushing = AtomicBoolean(false)
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    init {
        try {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        } catch (t: Throwable) {
            Log.w(TAG, "lifecycle observer not registered: ${t.message}")
        }
    }

    fun enqueue(event: AnalyticsEvent) {
        val obj = JSONObject().apply {
            put("event_id", event.id)
            put("name", event.name)
            put("created_at", isoFormat.format(Date(event.timestampMs)))
            val props = JSONObject()
            for ((k, v) in event.properties) {
                props.put(k, v ?: JSONObject.NULL)
            }
            put("properties", props)
        }
        buffer.add(obj)
        if (buffer.size >= batchSize) flush()
    }

    fun flush() {
        if (buffer.isEmpty()) return
        executor.execute { drainAndUpload() }
    }

    private fun flushSync() {
        if (buffer.isEmpty()) return
        drainAndUpload()
    }

    override fun onPause(owner: LifecycleOwner) { flush() }
    override fun onStop(owner: LifecycleOwner) { flushSync() }
    override fun onDestroy(owner: LifecycleOwner) { flushSync() }
    override fun onStart(owner: LifecycleOwner) { flush() }

    private fun drainAndUpload() {
        if (!isFlushing.compareAndSet(false, true)) return
        try {
            val snapshot = buffer.toList()
            buffer.clear()
            for (i in snapshot.indices step batchSize) {
                val batch = snapshot.subList(i, minOf(i + batchSize, snapshot.size))
                uploadBatch(batch)
            }
        } finally {
            isFlushing.set(false)
        }
    }

    private fun uploadBatch(batch: List<JSONObject>) {
        try {
            val payload = JSONObject().apply { put("events", JSONArray(batch)) }
            val connection = URL(endpoint).openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true
            }
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }
            val code = connection.responseCode
            if (code !in 200..299) {
                if (debugLogging) Log.w(TAG, "upload failed HTTP $code, requeuing ${batch.size}")
                buffer.addAll(0, batch)
            } else if (debugLogging) {
                Log.d(TAG, "uploaded ${batch.size} events")
            }
            connection.disconnect()
        } catch (t: Throwable) {
            if (debugLogging) Log.w(TAG, "upload threw, requeuing ${batch.size}: ${t.message}")
            buffer.addAll(0, batch)
        }
    }

    companion object {
        private const val TAG = "CodeFlowAnalytics"
    }
}
