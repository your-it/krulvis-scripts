package org.powbot.krulvis.tempoross

import org.powbot.api.Color.BLACK
import org.powbot.api.Color.GREEN
import org.powbot.api.Color.ORANGE
import org.powbot.api.Color.RED
import org.powbot.api.Tile
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics

class TemporossPainter(script: Tempoross) : ATPainter<Tempoross>(script, 12, 250) {

    override fun paint(g: Graphics) {
        var y = this.y
        val blockedTiles = script.blockedTiles.toList()
        val paths = script.triedPaths.toList()
        val spot = me.interacting()

        drawSplitText(g, "Leaf: ", "${script.lastLeaf.name}", x, y)
        y += yy
        if (debugComponents) {
            drawSplitText(g, "My Animation: ", "${me.animation()}", x, y)
            y += yy
            drawSplitText(g, "Interacting Anim: ", "${spot?.animation() ?: -1}", x, y)
            y += yy
            drawSplitText(g, "Side: ", "${script.side}", x, y)
            y += yy
            drawSplitText(g, "Energy: ${script.getEnergy()}", "Health: ${script.getHealth()}: ", x, y)
            y += yy
            drawSplitText(g, "Wave timer: ", script.waveTimer.getRemainderString(), x, y)
            y += yy
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

        if (spot != null && spot.name() == "Fishing spot") {
            drawTile(g, spot.tile())
        }



        drawSplitText(
            g,
            "Reward credits: ",
            "${script.rewardGained}, ${script.timer.getPerHour(script.rewardGained)}/hr",
            x,
            y
        )
        y += yy
        if (script.rounds > 0) {
            drawSplitText(
                g,
                "Points obtained: ",
                "${script.pointsObtained / script.rounds}/round",
                x,
                y
            )
        }
        script.skillTracker.draw(g, x, y)
    }

    override fun drawTitle(g: Graphics, x: Int, y: Int) {
        drawTitle(g, "Tempoross", x - 10, y - 4)
    }
}