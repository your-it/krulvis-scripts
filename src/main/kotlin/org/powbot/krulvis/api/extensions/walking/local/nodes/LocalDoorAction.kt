package org.powbot.krulvis.api.extensions.walking.local.nodes

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powerbot.script.Tile
import org.powerbot.script.rt4.GameObject


class LocalDoorAction(val door: GameObject, parent: LocalWebAction, destination: Tile, finalDestination: Tile) :
    LocalTileAction(parent, destination, finalDestination) {

    override val type: WebActionType =
        WebActionType.DOOR

    override fun execute(ctx: ATContext): Boolean {
        return if (ctx.interact(door, "Open")) {
            val openedDoor = waitFor { openedDoor(ctx) }
            println("Opened door: $openedDoor")
            openedDoor
        } else {
            false
        }
    }

    fun openedDoor(ctx: ATContext): Boolean {
        return ctx.objects.toStream().at(door)
            .noneMatch { ctx.lpf.isDoor(it) && it.orientation() == door.orientation() }
    }

    override fun toString(): String {
        return "Door action: ${door.name()}: @ ${door.tile()}"
    }
}