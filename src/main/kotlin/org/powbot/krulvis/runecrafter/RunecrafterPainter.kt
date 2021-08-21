package org.powbot.krulvis.runecrafter

import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics

class RunecrafterPainter(script: Runecrafter) : ATPainter<Runecrafter>(script, 10, 300) {
    override fun paint(g: Graphics, startY: Int): Int {
        var y = startY
        drawSplitText(g, "Leaf: ", script.lastLeaf.toString(), x, y)
        y = script.lootTracker.drawLoot(g, x, y)
        y = script.skillTracker.draw(g, x, y)
        return y
    }

    override fun drawTitle(g: Graphics, x: Int, y: Int) {
        drawTitle(g, "krul Runes", x, y)

    }
}