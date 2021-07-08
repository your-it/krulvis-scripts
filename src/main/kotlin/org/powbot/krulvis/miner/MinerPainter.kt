package org.powbot.krulvis.miner

import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.ATContext.mapPoint
import org.powbot.krulvis.api.extensions.items.Ore.Companion.getOre
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.painter.ATPainter.Companion.drawOnScreen
import org.powerbot.script.Tile
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import java.awt.geom.Ellipse2D

class MinerPainter(script: Miner) : ATPainter<Miner>(script, 10, 250) {
    override fun paint(g: Graphics2D) {
        var y = this.y
        val gui = script.gui
        if (!script.started && gui != null) {
            val ct = gui.centerTile
            if (ct != null) {
                ct.drawOnMap(g)
                ct.drawCircleOnMap(g, gui.radius)
            }
            gui.oreLocations.forEach { tile ->
                drawTile(g, tile)
                val matrix = tile.matrix(script.ctx).bounds()
                script.ctx.objects.toStream().at(tile).name("Rocks").findFirst().ifPresent {
                    val ore = it.getOre()
                    g.drawString(
                        ore?.name ?: "Not found",
                        matrix.bounds.centerX.toInt(),
                        matrix.bounds.centerY.toInt()
                    )
                }
            }
        } else if (!script.started && (gui == null || !gui.isVisible)) {
            drawSplitText(g, "Loading GUI", "...", x, y)
        } else {
            drawSplitText(g, "Leaf: ", script.lastLeaf.name, x, y)
            y += yy
            drawSplitText(g, "Should empty sack: ", script.shouldEmptySack.toString(), x, y)
            y += yy
            y = script.skillTracker.draw(g, x, y)
            y = script.lootTracker.drawLoot(g, x, y)

            if (debugComponents) {
                script.profile.oreLocations.forEach { tile ->
                    script.ctx.objects.toStream().at(tile).name("Rocks").findFirst().ifPresent {
                        val ore = it.getOre()
                        val tile = it.tile()
                        if (ore != null) {
                            tile.drawOnScreen(g, ore.name, Color.GREEN)
                        } else {
                            tile.drawOnScreen(
                                g,
                                if (it.modifiedColors().isNotEmpty()) "Unknown" else "Empty",
                                Color.RED
                            )
                        }
                    }
                }
            }
//            facingTile().drawOnScreen(g, outlineColor = Color.ORANGE)
        }
    }

    override fun drawTitle(g: Graphics2D, x: Int, y: Int) {
        super.drawTitle(g, x + 20, y)
    }

    override fun drawProgressImage(g: Graphics2D, startY: Int) {
        var y = startY
        y = script.skillTracker.draw(g, x, y)
        y = script.lootTracker.drawLoot(g, x, y)
    }
}