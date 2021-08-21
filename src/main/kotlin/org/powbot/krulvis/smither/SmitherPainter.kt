package org.powbot.krulvis.smither

import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics

class SmitherPainter(script: Smither) : ATPainter<Smither>(script, 6, 350) {
    override fun paint(g: Graphics, startY: Int): Int {
        var y = startY
        y = drawSplitText(g, "Leaf: ", script.lastLeaf.name, x, y)
        y = script.skillTracker.draw(g, x, y)
        return y
    }
}