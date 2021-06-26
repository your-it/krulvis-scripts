package org.powbot.krulvis.tempoross

import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powerbot.script.Tile
import java.awt.Color
import java.awt.Graphics2D

class TemporossPainter(script: Tempoross) : ATPainter<Tempoross>(script, 12, 250) {

    override fun paint(g: Graphics2D) {
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
                if (t != Tile.NIL) {
                    it.drawOnScreen(g, null, Color.RED)
                }
            }
            if (blockedTiles.isNotEmpty() && paths.isNotEmpty()) {
                paths.map { it.actions.map { a -> a.destination } }.forEach { tiles ->
                    val dangerous = tiles.any { script.blockedTiles.contains(it) }
                    tiles.forEach { tile ->
                        tile.drawOnScreen(
                            g,
                            null,
                            if (blockedTiles.contains(tile)) Color.BLACK else if (dangerous) Color.ORANGE else Color.GREEN
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

    override fun drawTitle(g: Graphics2D, x: Int, y: Int) {
        drawTitle(g, "Tempoross", x - 10, y - 4)
    }

    override fun drawProgressImage(g: Graphics2D, startY: Int) {
        var y = startY
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
        y = script.skillTracker.draw(g, x, y)
    }
}