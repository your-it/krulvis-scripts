package org.powbot.krulvis.api.extensions.walking.local.nodes

import org.powbot.api.Random
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.extensions.walking.PathFinder.Companion.isRockfall
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import java.util.*


class LocalRockfallEdge(parent: LocalEdge, destination: Tile) :
    LocalTileEdge(parent, destination) {

    override val type: LocalEdgeType =
        LocalEdgeType.ROCKFALL

    override fun getCost(): Double {
        if (destination == finalDestination) {
            return 0.0
        }
        return 5.0
    }

    override fun execute(): Boolean {
        val rocks = getRockfall()
        return if (rocks != null && interact(rocks, "Mine")) {
            val minedDoor = waitFor(Random.nextInt(4000, 6000)) { getRockfall() == null }
            println("Mined rock: ${getRockfall() == null}")
            minedDoor
        } else {
            println("Failed to mine rockfall...")
            false
        }

    }

    fun getRockfall() = Objects.stream(10).at(destination).filter { it.isRockfall() }.firstOrNull()

    override fun toString(): String {
        return "LocalRockfallEdge: @ ${destination.tile()}"
    }
}