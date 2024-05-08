package org.powbot.krulvis.api.script.painter

import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.paint.TrackInventoryOption
import org.powbot.krulvis.api.script.ATScript
import org.powbot.mobile.drawing.Rendering

abstract class ATPaint<S : ATScript>(val script: S, val x: Int = 110, val y: Int = 70) {

    val paintBuilder = PaintBuilder()

    init {
        paintBuilder.x(x)
        paintBuilder.y(y)
        paintBuilder.addString("Leaf: ") { script.lastLeaf.name }
    }

    fun PaintBuilder.trackInventoryItemQ(itemId: Int) = trackInventoryItem(itemId = itemId, textSize = null, TrackInventoryOption.QuantityChange)

    abstract fun buildPaint(paintBuilder: PaintBuilder): Paint

    open fun paintCustom(g: Rendering) {}
}