package org.powbot.krulvis.api.extensions

import org.powbot.api.rt4.Widget

object TargetWidget {

    fun widget() = Widget(303)

    fun name(): String =
        widget().components().firstOrNull { it.visible() && it.text().isNotEmpty() && !it.text().contains("/") }?.text()
            ?: "None"

    fun health(): Int {
        val text = widget().components().firstOrNull { it.visible() && it.text().contains("/") }?.text() ?: return -1
        return text.substring(0, text.indexOf("/") - 1).toInt()
    }
}