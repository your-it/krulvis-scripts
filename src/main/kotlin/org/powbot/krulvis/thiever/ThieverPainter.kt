package org.powbot.krulvis.thiever

import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics

class ThieverPainter(script: Thiever) : ATPainter<Thiever>(script, 10, 300) {
    override fun paint(g: Graphics, startY: Int): Int {
        var y = startY
        y = drawSplitText(g, "Leaf:", script.lastLeaf.name, x, y)
        y = drawSplitText(g, "Food: ${script.food}", "Amount: ${script.foodAmount}", x, y)
        y = script.skillTracker.draw(g, x, y)
        return y
    }
}