package org.powbot.krulvis.miner

import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics


class MinerPainter(script: Miner) : ATPainter<Miner>(script, 10, 350) {
    override fun paint(g: Graphics) {
        var y = this.y

        y = drawSplitText(g, "Leaf: ", script.lastLeaf.name, x, y)
        y = drawSplitText(g, "Should empty sack: ", script.shouldEmptySack.toString(), x, y)
        y = script.skillTracker.draw(g, x, y)
        y = script.lootTracker.drawLoot(g, x, y)

    }
}