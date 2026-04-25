package com.codeflow.sdk.analytics.internal

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

internal class ActivityTracker : Application.ActivityLifecycleCallbacks {

    @Volatile var currentActivityName: String? = null
        private set

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        val window = activity.window ?: return
        val current = window.callback ?: return
        if (current is WindowCallbackProxy) return
        try {
            window.callback = WindowCallbackProxy(current, activity)
        } catch (t: Throwable) {
            Log.w("CodeFlowAnalytics", "failed to wrap window callback: ${t.message}")
        }
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        currentActivityName = activity.javaClass.simpleName
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
