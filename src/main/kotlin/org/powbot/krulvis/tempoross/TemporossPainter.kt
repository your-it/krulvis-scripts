package org.powbot.krulvis.tempoross

import org.powbot.api.Color
import org.powbot.api.Color.BLACK
import org.powbot.api.Color.GREEN
import org.powbot.api.Color.ORANGE
import org.powbot.api.Color.RED
import org.powbot.api.Tile
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics

class TemporossPainter(script: Tempoross, lines: Int) : ATPainter<Tempoross>(script, lines, 350) {

    init {
        x = 1425
        y = 40
    }

    override fun bgColor(): Int = Color.argb(150, 0, 0, 0)

    override fun paint(g: Graphics, startY: Int): Int {
        var y = startY
        val blockedTiles = script.blockedTiles.toList()
        val paths = script.triedPaths.toList()
        val spot = me.interacting()

        y = drawSplitText(g, "Leaf: ", script.lastLeaf.name, x, y)
        if (!script.waveTimer.isFinished()) {
            y = drawSplitText(g, "Wave timer: ", script.waveTimer.getRemainderString(), x, y)
        }
        if (debugComponents) {
            y = drawSplitText(g, "My Animation: ", "${me.animation()}", x, y)
            y = drawSplitText(g, "Interacting Anim: ", "${spot?.animation() ?: -1}", x, y)
            y = drawSplitText(g, "Side: ", "${script.side}", x, y)
            y = drawSplitText(g, "Energy: ${script.getEnergy()}", "Health: ${script.getHealth()}: ", x, y)
            blockedTiles.forEach {
                val t = it
                if (t != Tile.Nil) {
                    it.drawOnScreen(g, null, RED)
                }
            }
            if (blockedTiles.isNotEmpty() && paths.isNotEmpty()) {
                paths.map { it.actions.map { a -> a.destination } }.forEach { tiles ->
                    val dangerous = tiles.any { script.blockedTiles.contains(it) }
                    tiles.forEach { tile ->
                        tile.drawOnScreen(
                            g,
                            null,
                            if (blockedTiles.contains(tile)) BLACK else if (dangerous) ORANGE else GREEN
                        )
                    }
                }
            }
        }

        y = drawSplitText(
            g,
            "Reward credits: ",
            "${script.rewardGained}, ${script.timer.getPerHour(script.rewardGained)}/hr",
            x,
            y
        )
        if (script.rounds > 0) {
            y = drawSplitText(
                g,
                "Points obtained: ",
                "${script.pointsObtained / script.rounds}/round",
                x,
                y
            )
        }
        y = script.skillTracker.draw(g, x, y)
        if (spot != null && spot.name() == "Fishing spot") {
            drawTile(g, spot.tile())
        }
        return y
    }

    override fun drawTitle(g: Graphics, x: Int, y: Int) {
        drawTitle(g, "Tempoross", x - 10, y - 4)
    }
}