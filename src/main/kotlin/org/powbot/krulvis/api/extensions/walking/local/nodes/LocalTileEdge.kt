package org.powbot.krulvis.api.extensions.walking.local.nodes

import org.powbot.krulvis.api.ATContext.ctx
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder.getLocalNeighbors
import org.powerbot.script.Condition
import org.powerbot.script.Tile
import org.powerbot.script.rt4.ClientContext


open class LocalTileEdge(
    override val parent: LocalEdge,
    destination: Tile,
    finalDestination: Tile
) :
    LocalEdge(destination, finalDestination) {

    override val type: LocalEdgeType =
        LocalEdgeType.WALKING

    override fun getCost(): Double {
        if (destination == finalDestination) {
            return 0.0
        }
        return 1.0
    }

    override fun getNeighbors(): MutableList<LocalEdge> {
        return getLocalNeighbors(finalDestination)
    }

    override fun execute(): Boolean {
        if (destination.distance() <= 1) {
            val matrix = destination.matrix(org.powerbot.script.ClientContext.ctx())
            return if (ctx.input.move(matrix.nextPoint())) {
                ctx.input.click(true)
                Condition.wait({
                    destination.distance() == 0.0
                }, 500, 5)
            } else {
                false
            }
        }
        return ctx.movement.step(destination)
    }

    override fun toString(): String {
        return "LocalTileEdge: $destination"
    }

}