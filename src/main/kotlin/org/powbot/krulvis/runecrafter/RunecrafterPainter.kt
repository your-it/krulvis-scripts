package org.powbot.krulvis.runecrafter

import org.powbot.krulvis.api.script.painter.ATPainter
import java.awt.Graphics2D

class RunecrafterPainter(script: Runecrafter) : ATPainter<Runecrafter>(script, 10, 300) {
    override fun paint(g: Graphics2D) {
        var y = this.y
        drawSplitText(g, "Leaf: ", script.lastLeaf.toString(), x, y)
        y = script.lootTracker.drawLoot(g, x, y)
        y = script.skillTracker.draw(g, x, y)
    }

    override fun drawProgressImage(g: Graphics2D, startY: Int) {
        TODO("Not yet implemented")
    }

    override fun drawTitle(g: Graphics2D, x: Int, y: Int) {
        drawTitle(g, "krul Runes", x, y)

    }
}