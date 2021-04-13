package org.powbot.krulvis.api.extensions.walking.local

import org.powbot.krulvis.api.extensions.walking.Flag
import org.powbot.krulvis.api.extensions.walking.Path
import org.powbot.krulvis.api.extensions.walking.local.nodes.LocalDoorAction
import org.powbot.krulvis.api.extensions.walking.local.nodes.LocalWebAction
import org.powbot.krulvis.api.extensions.walking.local.nodes.WebActionType
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powerbot.bot.rt4.client.internal.ICollisionMap
import org.powerbot.script.Tile
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import java.awt.geom.Line2D

class LocalPath(val actions: List<LocalWebAction>, override val script: ATScript) : Path() {

    override fun traverse(): Boolean = actions.traverse()

    fun isEmpty(): Boolean = actions.isEmpty()

    fun isNotEmpty(): Boolean = actions.isNotEmpty()

    fun containsSpecialNode(): Boolean {
        return actions.any { it.type != WebActionType.WALKING }
    }

    override fun finalDestination(): Tile = actions.last().destination

    private fun List<LocalWebAction>.traverse(): Boolean {
        if (isEmpty()) {
            return true
        }
//        println("Is there door: ${any { it is LocalDoorAction }}")
        val cutOff = indexOf(minBy { it.destination.distance() })
        val remainder = subList(cutOff, size)
//        println("Cutting of at: $cutOff of $size total, ${remainder.size} left")
        val firstSpecial = remainder.firstOrNull { it.type != WebActionType.WALKING }
        if (firstSpecial != null) {
            debug("Found special: $firstSpecial")
            if (firstSpecial.destination.distance() > 5) {
                firstSpecial.parent.execute(script)
            } else {
                firstSpecial.execute(script)
            }
            return false
        } else {
            val possibleOptions = remainder.filter { it.destination.distance() <= 13 }
//            possibleOptions.forEach { println(it.destination) }
            return possibleOptions.lastOrNull()?.execute(script) ?: false
        }
    }

    fun draw(g: Graphics2D) = actions.draw(g)

    private fun List<LocalWebAction>.draw(g: Graphics2D) {
        val flags = script.ctx.client().collisionMaps[me.tile().floor()]
        forEach {
            if (it is LocalDoorAction) {
                ATPainter.drawTileOnScreen(g, it.destination, Color.CYAN, null)
            } else {
                it.destination.drawCollisions(g, flags)
            }
        }
    }

    val size = actions.size

    fun Tile.drawCollisions(g: Graphics2D, flags: ICollisionMap) {
        val bounds = matrix(ctx).bounds() ?: return
        if (blocked(flags)) {
            ATPainter.drawTileOnScreen(g, this, null, Color.RED)
        } else {
            val flag = getFlag(flags)
            val south = Line2D.Double(
                Point(bounds.xpoints[0], bounds.ypoints[0]),
                Point(bounds.xpoints[1], bounds.ypoints[1])
            )
            val east = Line2D.Double(
                Point(bounds.xpoints[1], bounds.ypoints[1]),
                Point(bounds.xpoints[2], bounds.ypoints[2])
            )
            val north = Line2D.Double(
                Point(bounds.xpoints[2], bounds.ypoints[2]),
                Point(bounds.xpoints[3], bounds.ypoints[3])
            )
            val west = Line2D.Double(
                Point(bounds.xpoints[3], bounds.ypoints[3]),
                Point(bounds.xpoints[0], bounds.ypoints[0])
            )
            g.color = if (flag and Flag.W_S == 0) Color.GREEN else Color.RED
            g.draw(south)

            g.color = if (flag and Flag.W_E == 0) Color.GREEN else Color.RED
            g.draw(east)

            g.color = if (flag and Flag.W_N == 0) Color.GREEN else Color.RED
            g.draw(north)

            g.color = if (flag and Flag.W_W == 0) Color.GREEN else Color.RED
            g.draw(west)
        }
    }


}