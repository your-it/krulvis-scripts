package org.powbot.krulvis.api.extensions.walking.local.nodes

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.extensions.walking.PathFinder
import org.powbot.krulvis.api.extensions.walking.PathFinder.Companion.isDoor
import org.powbot.krulvis.api.utils.Utils.waitFor


class LocalDoorEdge(val door: GameObject, parent: LocalEdge, destination: Tile) :
    LocalTileEdge(parent, destination) {

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
        return Objects.stream().at(door)
            .noneMatch { it.isDoor() && it.orientation() == door.orientation() }
    }

    override fun toString(): String {
        return "LocalDoorEdge: ${door.name()} @ ${door.tile()}"
    }
}