package org.powbot.krulvis.api.extensions.walking.local.nodes

import org.powbot.api.Condition
import org.powbot.api.Input
import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder.getLocalNeighbors


open class LocalTileEdge(
    override val parent: LocalEdge,
    destination: Tile
) :
    LocalEdge(destination, parent.finalDestination) {

    override val type: LocalEdgeType =
        LocalEdgeType.WALKING

    override fun getCost(): Double {
        if (destination == finalDestination) {
            return 0.0
        }
        return 1.0
    }

    override fun execute(): Boolean {
        if (destination.distance() <= 2) {
            val matrix = destination.matrix()
            return if (matrix?.click() == true) {
                Condition.wait({
                    destination.distance() == 0.0
                }, 500, 5)
            } else {
                false
            }
        }
        return Movement.step(destination)
    }

    override fun toString(): String {
        return "LocalTileEdge: $destination"
    }

}