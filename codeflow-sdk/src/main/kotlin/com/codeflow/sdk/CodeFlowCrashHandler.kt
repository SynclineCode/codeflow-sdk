package com.codeflow.sdk

import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Process-wide crash handler. Once [install]ed it becomes the default
 * uncaught-exception handler, so it catches crashes on *every* thread in the
 * process (main, background, pools — anything that dies with an uncaught
 * Throwable). Each crash is logged through [CodeFlowLogger] as a FATAL entry and
 * uploaded before the process is allowed to die.
 *
 * The previously-registered handler (typically the Android runtime handler that
 * shows the "app has stopped" dialog and kills the process) is preserved and
 * invoked afterwards, so normal crash behaviour is unchanged — we just get a log
 * out first.
 */
object CodeFlowCrashHandler : Thread.UncaughtExceptionHandler {

    private const val TAG = "CodeFlowCrashHandler"

    /** How long to wait for the crash upload before giving up and letting the process die. */
    private const val UPLOAD_TIMEOUT_MS = 5_000L

    private val installed = AtomicBoolean(false)
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    /**
     * Install the handler. Idempotent — calling it more than once is a no-op.
     * Requires [CodeFlowLogger.init] to have been called for crashes to be uploaded.
     */
    fun install() {
        if (!installed.compareAndSet(false, true)) return
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        CodeFlowLogger.info(TAG, "Crash handler installed")
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // Upload on a dedicated thread: the crashing thread is often the main
            // thread (NetworkOnMainThreadException) and is about to be torn down.
            // We join with a timeout so the report has a bounded window to land
            // before we hand control to the previous handler, which kills the process.
            val uploader = Thread({
                try {
                    CodeFlowLogger.reportCrash(thread, throwable)
                } catch (t: Throwable) {
                    Log.e(TAG, "Crash upload failed", t)
                }
            }, "codeflow-crash-uploader")
            uploader.start()
            uploader.join(UPLOAD_TIMEOUT_MS)
        } catch (t: Throwable) {
            // Never let the crash handler itself throw — that would mask the original crash.
            Log.e(TAG, "Failed to handle uncaught exception", t)
        } finally {
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}
