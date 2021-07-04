package org.powbot.krulvis.api.extensions.walking.local.nodes

import org.powbot.krulvis.api.ATContext.ctx
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.extensions.walking.PathFinder.Companion.isRockfall
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powerbot.script.Tile
import org.powerbot.script.rt4.GameObject
import java.util.*


class LocalRockfallEdge(parent: LocalEdge, destination: Tile, finalDestination: Tile) :
    LocalTileEdge(parent, destination, finalDestination) {

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
        return if (rocks.isPresent && interact(rocks.get(), "Mine")) {
            val minedDoor = waitFor { getRockfall().isEmpty }
            println("Mined rock: $minedDoor")
            minedDoor
        } else {
            false
        }

    }

    fun getRockfall(): Optional<GameObject> =
        ctx.objects.toStream().at(destination).filter { it.isRockfall() }.findFirst()

    override fun toString(): String {
        return "LocalRockfallEdge: @ ${destination.tile()}"
    }
}