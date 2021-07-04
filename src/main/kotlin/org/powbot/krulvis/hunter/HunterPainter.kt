package org.powbot.krulvis.hunter

import org.powbot.krulvis.api.script.painter.ATPainter
import java.awt.Graphics2D

class HunterPainter(script: Hunter) : ATPainter<Hunter>(script, 10) {
    override fun paint(g: Graphics2D) {
        var y = this.y
        drawSplitText(g, "Last leaf:", script.lastLeaf.name, x, y)
        y += yy
        y = script.skillTracker.draw(g, x, y)
    }

    override fun drawProgressImage(g: Graphics2D, startY: Int) {

    }
}