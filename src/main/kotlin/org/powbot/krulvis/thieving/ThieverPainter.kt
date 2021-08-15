package org.powbot.krulvis.thieving

import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics

class ThieverPainter(script: Thiever) : ATPainter<Thiever>(script, 10, 250) {
    override fun paint(g: Graphics) {
        var y = this.y
        y = script.skillTracker.draw(g, x, y)
    }

    override fun drawProgressImage(g: Graphics, startY: Int) {
        TODO("Not yet implemented")
    }
}