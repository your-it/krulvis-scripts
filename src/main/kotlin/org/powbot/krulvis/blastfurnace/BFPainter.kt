package org.powbot.krulvis.blastfurnace

import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics
import java.awt.Graphics2D

class BFPainter(script: BlastFurnace) : ATPainter<BlastFurnace>(script, 12, 250) {
    override fun paint(g: Graphics) {
        var y = this.y
        val bar = script.bar
        val prim = bar.primary
        val coal = bar.secondary
        drawSplitText(g, "Leaf:", script.lastLeaf.name, x, y)
        y += yy
        drawSplitText(g, "Bars: ", bar.amount.toString(), x, y)
        y += yy
        drawSplitText(g, "${prim.name} Ore: ", prim.amount.toString(), x, y)
        y += yy
        drawSplitText(g, "Coal: ", coal.amount.toString(), x, y)

    }

    override fun drawProgressImage(g: Graphics, startY: Int) {
        TODO("Not yet implemented")
    }
}