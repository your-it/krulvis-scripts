package org.powbot.krulvis.api.extensions.walking.local.nodes

import org.powbot.krulvis.api.ATContext.ctx
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder.getLocalNeighbors
import org.powerbot.script.Tile


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
        return ctx.movement.step(destination)
    }

    override fun toString(): String {
        return "LocalTileEdge: $destination"
    }

}