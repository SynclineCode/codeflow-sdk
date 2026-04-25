package com.codeflow.sdk.analytics.internal

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsOwner
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import com.codeflow.sdk.analytics.AnalyticsEvent
import com.codeflow.sdk.analytics.CodeFlowAnalytics

internal object SemanticsHitTester {

    private const val ANDROID_COMPOSE_VIEW = "androidx.compose.ui.platform.AndroidComposeView"

    fun report(activity: Activity, root: View, x: Float, y: Float, longPress: Boolean) {
        val composeViews = ArrayList<View>(2)
        collectComposeViews(root, composeViews)
        if (composeViews.isEmpty()) return

        val loc = IntArray(2)
        for (composeView in composeViews) {
            val owner = readSemanticsOwner(composeView) ?: continue
            composeView.getLocationInWindow(loc)
            val rootNode = try {
                owner.unmergedRootSemanticsNode
            } catch (t: Throwable) {
                continue
            }
            val node = findInteractiveAt(rootNode, x, y) ?: continue
            emit(activity, node, longPress)
            return
        }
    }

    private fun collectComposeViews(view: View, out: MutableList<View>) {
        if (view.javaClass.name == ANDROID_COMPOSE_VIEW) {
            out.add(view)
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                collectComposeViews(view.getChildAt(i), out)
            }
        }
    }

    private fun readSemanticsOwner(view: View): SemanticsOwner? {
        return try {
            val getter = view.javaClass.getMethod("getSemanticsOwner")
            getter.invoke(view) as? SemanticsOwner
        } catch (t: Throwable) {
            null
        }
    }

    private fun findInteractiveAt(root: SemanticsNode, x: Float, y: Float): SemanticsNode? {
        var best: SemanticsNode? = null
        fun walk(node: SemanticsNode) {
            val b = node.boundsInWindow
            if (x < b.left || x > b.right || y < b.top || y > b.bottom) return
            if (isInteractive(node)) best = node
            for (child in node.children) walk(child)
        }
        walk(root)
        return best
    }

    private fun isInteractive(node: SemanticsNode): Boolean {
        val cfg = node.config
        return cfg.contains(SemanticsActions.OnClick) ||
            cfg.contains(SemanticsActions.OnLongClick) ||
            cfg.contains(SemanticsActions.SetText) ||
            cfg.contains(SemanticsActions.SetProgress) ||
            cfg.getOrNull(SemanticsProperties.ToggleableState) != null ||
            cfg.getOrNull(SemanticsProperties.Selected) != null
    }

    private fun emit(activity: Activity, node: SemanticsNode, longPress: Boolean) {
        val cfg = node.config
        val props = LinkedHashMap<String, Any?>()
        props["screen"] = activity.javaClass.simpleName

        val role = cfg.getOrNull(SemanticsProperties.Role)?.toString()
        props["role"] = role ?: inferRole(node)

        val maxLen = CodeFlowAnalytics.config()?.maxLabelChars ?: 80
        props["label"] = label(node).take(maxLen)

        cfg.getOrNull(SemanticsProperties.TestTag)?.let { props["test_tag"] = it }
        cfg.getOrNull(SemanticsProperties.ToggleableState)?.let { props["toggle_state"] = it.toString() }
        cfg.getOrNull(SemanticsProperties.Selected)?.let { props["selected"] = it }
        cfg.getOrNull(SemanticsProperties.StateDescription)?.let { props["state"] = it }
        cfg.getOrNull(SemanticsProperties.ProgressBarRangeInfo)?.let { p ->
            props["progress"] = p.current
            props["progress_min"] = p.range.start
            props["progress_max"] = p.range.endInclusive
        }
        cfg.getOrNull(SemanticsProperties.EditableText)?.text?.let {
            props["text"] = it.take(maxLen)
        }
        val b = node.boundsInWindow
        props["bounds"] = "${b.left.toInt()},${b.top.toInt()} ${b.width.toInt()}x${b.height.toInt()}"

        val name = if (longPress) "ui_long_press" else "ui_tap"
        CodeFlowAnalytics.track(AnalyticsEvent(name = name, properties = props))
    }

    private fun inferRole(node: SemanticsNode): String {
        val cfg = node.config
        return when {
            cfg.contains(SemanticsActions.SetText) -> "TextField"
            cfg.contains(SemanticsActions.SetProgress) -> "Slider"
            cfg.getOrNull(SemanticsProperties.ToggleableState) != null -> "Toggleable"
            cfg.getOrNull(SemanticsProperties.Selected) != null -> "Selectable"
            cfg.contains(SemanticsActions.OnClick) -> "Clickable"
            else -> "Unknown"
        }
    }

    private fun label(node: SemanticsNode): String {
        val cfg = node.config
        cfg.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull()?.let { v ->
            val s = v.trim()
            if (s.isNotEmpty()) return s
        }
        cfg.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.text?.trim()?.let { s ->
            if (s.isNotEmpty()) return s
        }
        cfg.getOrNull(SemanticsProperties.EditableText)?.text?.trim()?.let { s ->
            if (s.isNotEmpty()) return s
        }
        cfg.getOrNull(SemanticsProperties.TestTag)?.let { return it }
        for (child in node.children) {
            val l = label(child)
            if (l.isNotEmpty() && l != "<unlabeled>") return l
        }
        return "<unlabeled>"
    }
}
