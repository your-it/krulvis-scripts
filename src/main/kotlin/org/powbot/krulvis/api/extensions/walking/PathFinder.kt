package org.powbot.krulvis.api.extensions.walking

import org.powerbot.script.ClientContext
import org.powerbot.script.Tile
import org.powerbot.script.rt4.Constants
import org.powerbot.script.rt4.GameObject

interface PathFinder {

    fun canUseDoor(door: GameObject): Boolean {
        if (door.tile() == Tile(2611, 3394, 0)) {
            return ClientContext.ctx().skills.level(Constants.SKILLS_ATTACK) >= 68
        }
        return !blockedDoors.contains(door.tile())
    }

    fun isDoor(so: GameObject): Boolean {
        return doors.contains(so.name())
    }

    fun getDoor(tile: Tile, orientation: Int): GameObject {
        return ClientContext.ctx().objects.toStream().at(tile).action(actions).filter {
            it.name() != null && isDoor(it)
                    && it.orientation() == orientation
                    && canUseDoor(it)
        }.first()
    }

    companion object {
        val actions = listOf("Open", "Use", "Enter", "Pay")
        val doors = listOf(
            "Door",
            "Glass door",
            "Gate",
            "Large door",
            "Castle door",
            "Gate of War",
            "Rickety door",
            "Oozing barrier",
            "Portal of Death",
            "Magic guild door",
            "Prison door",
            "Barbarian door"
        )
        val blockedDoors = listOf(Tile(2515, 9575, 0), Tile(2568, 9893, 0), Tile(2566, 9901, 0), Tile(2924, 9803, 0))
    }

}