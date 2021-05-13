package org.powbot.krulvis.api.extensions.walking.local.nodes

import org.powbot.krulvis.api.ATContext.ctx
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.extensions.walking.PathFinder
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powerbot.script.Tile
import org.powerbot.script.rt4.GameObject


class LocalDoorEdge(val door: GameObject, parent: LocalEdge, destination: Tile, finalDestination: Tile) :
    LocalTileEdge(parent, destination, finalDestination) {

    override val type: LocalEdgeType =
        LocalEdgeType.DOOR

    override fun execute(): Boolean {
        val action = door.actions().firstOrNull { it in PathFinder.actions } ?: return false
        return if (interact(door, action)) {
            val openedDoor = waitFor { openedDoor() }
            println("Opened door: $openedDoor")
            openedDoor
        } else {
            false
        }
    }

    fun openedDoor(): Boolean {
        return ctx.objects.toStream().at(door)
            .noneMatch { LocalPathFinder.isDoor(it) && it.orientation() == door.orientation() }
    }

    override fun toString(): String {
        return "LocalDoorEdge: ${door.name()} @ ${door.tile()}"
    }
}