package org.powbot.krulvis.blastfurnace

import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics

class BFPainter(script: BlastFurnace) : ATPainter<BlastFurnace>(script, 8, 300) {
    override fun paint(g: Graphics, startY: Int): Int {
        var y = startY
        val bar = script.bar
        val prim = bar.primary
        val coal = bar.secondary
        y = drawSplitText(g, "Leaf:", script.lastLeaf.name, x, y)
        y = drawSplitText(g, "Bars: ", bar.blastFurnaceCount.toString(), x, y)
        y = drawSplitText(g, "${prim.name} Ore: ", prim.blastFurnaceCount.toString(), x, y)
        if (prim != Ore.GOLD) {
            y = drawSplitText(g, "Coal: ", coal.blastFurnaceCount.toString(), x, y)
        }
        y = script.skillTracker.draw(g, x, y)
        y = script.lootTracker.draw(g, x, y)
        return y
    }

    override fun drawTitle(g: Graphics, x: Int, y: Int) {
        Companion.drawTitle(g, "Blast Furnace", x, y)
    }
}