package org.powbot.krulvis.api.extensions.walking.local

import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.walking.Path
import org.powbot.krulvis.api.extensions.walking.local.nodes.LocalDoorEdge
import org.powbot.krulvis.api.extensions.walking.local.nodes.LocalEdge
import org.powbot.krulvis.api.extensions.walking.local.nodes.LocalEdgeType
import org.powbot.krulvis.api.script.painter.ATPainter.Companion.drawCollisions
import org.powbot.krulvis.api.script.painter.ATPainter.Companion.drawOnScreen
import org.powerbot.script.ClientContext
import org.powerbot.script.Tile
import java.awt.Color
import java.awt.Graphics2D
import java.util.logging.Logger

class LocalPath(val actions: List<LocalEdge>) : Path() {

    override fun traverse(): Boolean = actions.traverse()

    companion object {

        val logger = Logger.getLogger("LocalPath")

        fun List<LocalEdge>.getNext(): LocalEdge? {
            val cutOff = indexOf(minByOrNull { it.destination.distance() })
            val remainder = subList(cutOff, size)
            logger.info("Cutting of at: $cutOff of $size total, ${remainder.size} left")
            val firstSpecial = remainder.firstOrNull { it.type != LocalEdgeType.WALKING }
            if (firstSpecial != null) {
                logger.info("Found special: $firstSpecial")
                return firstSpecial
            } else {
                val possibleOptions = remainder.filter { it.destination.distance() <= 13 }
//            possibleOptions.forEach { println(it.destination) }
                return possibleOptions.lastOrNull() ?: null
            }
        }

        private fun List<LocalEdge>.traverse(): Boolean {
            if (isEmpty()) {
                return true
            }
            val next = getNext() ?: return false
            return if (next.type != LocalEdgeType.WALKING) {
                if (next.destination.distance() > 5) {
                    next.parent.execute()
                } else {
                    next.execute()
                }
                false
            } else {
                next.execute()
            }
        }
    }

    fun isEmpty(): Boolean = actions.isEmpty()

    fun isNotEmpty(): Boolean = actions.isNotEmpty()

    fun containsSpecialNode(): Boolean {
        return actions.any { it.type != LocalEdgeType.WALKING }
    }

    override fun finalDestination(): Tile = actions.last().destination

    fun draw(g: Graphics2D) = actions.draw(g)

    private fun List<LocalEdge>.draw(g: Graphics2D) {
        val flags = ClientContext.ctx().client().collisionMaps[me.tile().floor()]
        forEach {
            if (it is LocalDoorEdge) {
                it.destination.drawOnScreen(g, null, Color.CYAN, null)
            } else {
                it.destination.drawCollisions(g, flags)
            }
        }
    }

    val size = actions.size

}