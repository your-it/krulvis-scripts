package org.powbot.krulvis.tithe

import org.powbot.krulvis.api.script.painter.ATPainter
import java.awt.Color
import java.awt.Graphics2D

class TithePainter(script: TitheFarmer) : ATPainter<TitheFarmer>(script, 10, 250) {
    override fun paint(g: Graphics2D) {
        var y = this.y
        drawSplitText(g, "Last leaf: ", script.lastLeaf.name, x, y)
        y += yy
        drawSplitText(
            g,
            "Gained Points: ",
            "${script.gainedPoints}, (${script.timer.getPerHour(script.gainedPoints)}/hr)",
            x,
            y
        )
        y += yy

        y = script.skillTracker.draw(g, x, y)
//        val patches = script.patches
//        patches.forEach {
//            if (!it.isNill) {
//                val color =
//                    if (it.isNill) Color.BLACK else if (it.isEmpty()) Color.CYAN else if (it.isDone()) Color.GREEN else if (!it.needsWatering()) Color.YELLOW else Color.BLUE
//                drawTile(g, it.go.tile(), text = it.toString(), lineColor = color, mapColor = color)
//            }
//        }
    }

    override fun drawTitle(g: Graphics2D, x: Int, y: Int) {
        super.drawTitle(g, x, y)
    }

    override fun drawProgressImage(g: Graphics2D, startY: Int) {
        var y = startY
        drawSplitText(
            g,
            "Gained Points: ",
            "${script.gainedPoints}, (${script.timer.getPerHour(script.gainedPoints)}/hr)",
            x,
            y
        )
        y += yy
        y = script.skillTracker.draw(g, x, y)
        y = script.lootTracker.drawLoot(g, x, y)
    }
}