package com.codeflow.sdk

data class CodeFlowConfig(
    val applicationId: String,
    val debugLogging: Boolean = false,
    /**
     * When true (the default), [CodeFlowLogger.init] installs a process-wide
     * [CodeFlowCrashHandler] that captures uncaught exceptions on every thread
     * and uploads them as FATAL logs before the process dies.
     */
    val captureCrashes: Boolean = true
)
