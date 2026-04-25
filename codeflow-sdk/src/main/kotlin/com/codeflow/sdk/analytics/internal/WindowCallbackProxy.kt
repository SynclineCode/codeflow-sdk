package com.codeflow.sdk.analytics.internal

import android.app.Activity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.Window
import com.codeflow.sdk.analytics.CodeFlowAnalytics
import kotlin.math.hypot

internal class WindowCallbackProxy(
    private val delegate: Window.Callback,
    private val activity: Activity
) : Window.Callback by delegate {

    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L
    private var moved = false
    private val touchSlop = ViewConfiguration.get(activity).scaledTouchSlop.toFloat()
    private val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                downTime = event.eventTime
                moved = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!moved && hypot((event.x - downX).toDouble(), (event.y - downY).toDouble()) > touchSlop) {
                    moved = true
                }
            }
            MotionEvent.ACTION_UP -> {
                val cfg = CodeFlowAnalytics.config()
                if (cfg != null && !moved) {
                    val dt = event.eventTime - downTime
                    val isLongPress = dt > longPressTimeout
                    val capture = (isLongPress && cfg.captureLongPresses) ||
                        (!isLongPress && cfg.captureTaps)
                    if (capture) {
                        val x = event.x
                        val y = event.y
                        val decor = activity.window?.decorView
                        decor?.post {
                            try {
                                SemanticsHitTester.report(activity, decor, x, y, isLongPress)
                            } catch (t: Throwable) {
                                // never crash the host on analytics
                            }
                        }
                    }
                }
            }
        }
        return delegate.dispatchTouchEvent(event)
    }
}
