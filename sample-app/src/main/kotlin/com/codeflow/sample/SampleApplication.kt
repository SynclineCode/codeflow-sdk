package com.codeflow.sample

import android.app.Application
import com.codeflow.sdk.CodeFlowConfig
import com.codeflow.sdk.CodeFlowLogger
import com.codeflow.sdk.analytics.AnalyticsConfig
import com.codeflow.sdk.analytics.CodeFlowAnalytics

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // The only line of "instrumentation" anywhere in the app: a single
        // init call. From here on, every Compose tap/long-press is captured
        // automatically via the analytics module's Window.Callback hook.
        CodeFlowAnalytics.init(
            this,
            AnalyticsConfig(
                applicationId = "codeflow-sample",
                debugLogging = true,
            )
        )

        // Init the logger too. captureCrashes defaults to true, so this installs
        // the process-wide crash handler that uploads any uncaught exception as a
        // FATAL log before the process dies.
        CodeFlowLogger.init(
            CodeFlowConfig(
                applicationId = "codeflow-sample",
                debugLogging = true,
            )
        )
    }
}
