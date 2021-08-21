package org.powbot.krulvis.smelter

import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics

class SmelterPainter(script: Smelter) : ATPainter<Smelter>(script, 6, 350) {
    override fun paint(g: Graphics, startY: Int): Int {
        var y = startY
        y = drawSplitText(g, "Leaf: ", script.lastLeaf.name, x, y)
        if (script.options.all { it.configured }) {
            y = drawSplitText(g, "Bar: ", script.bar.toString(), x, y)
            y = drawSplitText(g, "SmeltableCount: ", script.bar.getSmeltableCount().toString(), x, y)
        }
        y = script.skillTracker.draw(g, x, y)
        return y
    }
}